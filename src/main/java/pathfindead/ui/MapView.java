package pathfindead.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pathfindead.model.Edge;
import pathfindead.model.Graph;
import pathfindead.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Lienzo donde se dibuja la ciudad isométrica abandonada con su grafo encima. */
public class MapView extends Canvas {

    // Tamaño de la cuadrícula: un poco más grande que las coordenadas de los nodos (0–15, 0–12)
    private static final int MIN_X = -1, MAX_X = 16;
    private static final int MIN_Y = -1, MAX_Y = 13;

    // Tamaño para el que se diseñó el dibujo; si el Canvas es más pequeño, todo se reduce
    private static final double DESIGN_W = 1100;
    private static final double DESIGN_H = 700;

    private static final double FLY_H = 60;    // altura de vuelo de los corredores aéreos
    private static final double WALL_H = 16;   // altura del muro que rodea la ciudad

    // ---------- Paleta postapocalíptica ----------
    private static final Color SAND = Color.web("#b3a685");
    private static final Color[] DIRT = {
            Color.web("#a99b79"), Color.web("#b1a382"), Color.web("#a0946f"), Color.web("#928c5e")
    };
    private static final Color SIDEWALK = Color.web("#a5a49b");
    private static final Color ASPHALT = Color.web("#46484b");
    private static final Color LANE = Color.web("#e6e1d3");
    private static final Color OUTLINE = Color.web("#2e2b26");
    private static final Color WALL = Color.web("#8e908c");
    private static final Color WALL_SIDE = Color.web("#73756f");
    private static final Color HAZARD_Y = Color.web("#e2b93b");
    private static final Color HAZARD_K = Color.web("#2b2b2b");
    private static final Color TRUNK = Color.web("#4e3d2f");
    private static final Color SAFE = Color.web("#3fae49");
    private static final Color MEDIUM = Color.web("#f2c94c");
    private static final Color DANGER = Color.web("#e53935");

    // Colores de edificios abandonados: {techo, cara izquierda, cara derecha}
    private static final Color[][] BUILDING_COLORS = {
            {Color.web("#8fa2a3"), Color.web("#6f8384"), Color.web("#5a6b6c")},   // verde grisáceo
            {Color.web("#6f9aa0"), Color.web("#4f7c82"), Color.web("#3e6469")},   // verde azulado
            {Color.web("#cdbd9c"), Color.web("#b3a283"), Color.web("#978768")},   // beige
            {Color.web("#9a9d9e"), Color.web("#7c7f80"), Color.web("#646768")},   // gris
            {Color.web("#7f8ea0"), Color.web("#647386"), Color.web("#4f5c6c")}    // azul grisáceo
    };
    // Colores de contenedores: {techo, izquierda, derecha}
    private static final Color[][] CONTAINER_COLORS = {
            {Color.web("#bf5146"), Color.web("#a8423a"), Color.web("#86342d")},   // rojo
            {Color.web("#4f82b0"), Color.web("#3f6e9a"), Color.web("#30577b")},   // azul
            {Color.web("#5f9e6b"), Color.web("#4f8a5b"), Color.web("#3d6d47")},   // verde
            {Color.web("#d2843f"), Color.web("#c0703a"), Color.web("#985930")}    // naranja
    };
    private static final Color[] CAR_COLORS = {
            Color.web("#8b4a3c"), Color.web("#4f6d8a"), Color.web("#6b6b45"),
            Color.web("#bdb9ad"), Color.web("#b8743a"), Color.web("#3a3632")
    };

    /** Tipos de objetos de decoración que puede tener una casilla. */
    private enum Kind { BUILDING, CONTAINERS, TREE, BARRELS, RUBBLE }

    /** Objeto de decoración: su tipo, un número para variar su aspecto y su altura. */
    private record Prop(Kind kind, int variant, int height) { }

    private final Graph city;
    private final IsoProjection iso = IsoProjection.forCity();

    private final Map<String, Node> nodeAt = new HashMap<>();       // "x,y" -> nodo
    private final Map<String, Prop> props = new HashMap<>();        // "x,y" -> decoración
    private final Map<String, Integer> cars = new HashMap<>();      // "x,y" -> carro abandonado
    private final Map<Edge, double[]> curveControl = new HashMap<>();
    private final Map<Edge, double[]> labelPos = new HashMap<>();

    private boolean showCosts = true;

    public MapView(Graph city, double width, double height) {
        super(width, height);
        this.city = city;
        for (Node n : city.getNodes()) {
            nodeAt.put(key((int) Math.round(n.getX()), (int) Math.round(n.getY())), n);
        }
        generateProps();
        computeCurves();
        placeLabels();
        draw();
    }

    public void setShowCosts(boolean showCosts) {
        this.showCosts = showCosts;
        draw();
    }

    // =====================================================================
    // Datos del mapa
    // =====================================================================

    private static String key(int gx, int gy) {
        return gx + "," + gy;
    }

    /** Calles: columnas 3, 8, 12 y filas 3, 8 (ningún nodo cae en ellas). */
    private static boolean isStreet(int gx, int gy) {
        return gx == 3 || gx == 8 || gx == 12 || gy == 3 || gy == 8;
    }

    private static boolean isIntersection(int gx, int gy) {
        return (gx == 3 || gx == 8 || gx == 12) && (gy == 3 || gy == 8);
    }

    /** Acera: casilla que no es calle pero toca una calle. */
    private static boolean isSidewalk(int gx, int gy) {
        return !isStreet(gx, gy) && (isStreet(gx + 1, gy) || isStreet(gx - 1, gy)
                || isStreet(gx, gy + 1) || isStreet(gx, gy - 1));
    }

    /** true si la casilla está justo delante de un nodo (ahí no se ponen cosas altas que lo tapen). */
    private boolean inFrontOfNode(int gx, int gy) {
        return nodeAt.containsKey(key(gx - 1, gy)) || nodeAt.containsKey(key(gx, gy - 1))
                || nodeAt.containsKey(key(gx - 1, gy - 1));
    }

    /** Reparte edificios, contenedores, árboles, barriles, escombros y carros. Semilla fija = mismo mapa siempre. */
    private void generateProps() {
        Random rnd = new Random(7);
        for (int gx = MIN_X; gx <= MAX_X; gx++) {
            for (int gy = MIN_Y; gy <= MAX_Y; gy++) {
                double roll = rnd.nextDouble();
                int variant = rnd.nextInt(1000);
                int height = 16 + rnd.nextInt(30);
                String k = key(gx, gy);
                if (nodeAt.containsKey(k)) continue;

                if (isStreet(gx, gy)) {
                    if (!isIntersection(gx, gy) && roll < 0.14) cars.put(k, variant);
                    continue;
                }
                boolean front = inFrontOfNode(gx, gy);
                if (roll < 0.22 && !front) {
                    props.put(k, new Prop(Kind.BUILDING, variant, height));
                } else if (roll < 0.29) {
                    props.put(k, new Prop(Kind.CONTAINERS, variant, 0));
                } else if (roll < 0.36 && !front) {
                    props.put(k, new Prop(Kind.TREE, variant, 0));
                } else if (roll < 0.41) {
                    props.put(k, new Prop(Kind.BARRELS, variant, 0));
                } else if (roll < 0.48) {
                    props.put(k, new Prop(Kind.RUBBLE, variant, 0));
                }
            }
        }
    }

    // ---------- Ayudas de posición ----------

    private double sx(Node n) { return iso.toScreenX(n.getX(), n.getY()); }
    private double sy(Node n) { return iso.toScreenY(n.getX(), n.getY()); }

    /** Píxel X de un punto de la cuadrícula. */
    private double px(double gx, double gy) { return iso.toScreenX(gx, gy); }

    /** Píxel Y de un punto de la cuadrícula a una altura z sobre el suelo. */
    private double py(double gx, double gy, double z) { return iso.toScreenY(gx, gy) - z; }

    // =====================================================================
    // Geometría de los corredores (rectos o curvos)
    // =====================================================================

    private double[] pointOn(Edge e, double t) {
        double x1 = sx(e.getA()), y1 = sy(e.getA()) - FLY_H;
        double x2 = sx(e.getB()), y2 = sy(e.getB()) - FLY_H;
        double[] c = curveControl.get(e);
        if (c == null) {
            return new double[]{x1 + (x2 - x1) * t, y1 + (y2 - y1) * t};
        }
        double u = 1 - t;
        return new double[]{
                u * u * x1 + 2 * u * t * c[0] + t * t * x2,
                u * u * y1 + 2 * u * t * c[1] + t * t * y2
        };
    }

    private double[] controlPoint(Edge e, double bend) {
        double x1 = sx(e.getA()), y1 = sy(e.getA()) - FLY_H;
        double x2 = sx(e.getB()), y2 = sy(e.getB()) - FLY_H;
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        double nx = -dy / len, ny = dx / len;
        if (ny > 0) {
            nx = -nx;
            ny = -ny;
        }
        return new double[]{(x1 + x2) / 2 + nx * 2 * bend, (y1 + y2) / 2 + ny * 2 * bend};
    }

    private double minNodeDistance(Edge e) {
        double min = Double.MAX_VALUE;
        for (Node n : city.getNodes()) {
            if (e.connects(n)) continue;
            double nx = sx(n), ny = sy(n) - FLY_H;
            for (int i = 4; i <= 56; i++) {
                double[] p = pointOn(e, i / 60.0);
                min = Math.min(min, Math.hypot(p[0] - nx, p[1] - ny));
            }
        }
        return min;
    }

    private void computeCurves() {
        for (Edge e : city.getAllEdges()) {
            if (minNodeDistance(e) >= 14) continue;
            for (double bend : new double[]{40, -40, 30, -30, 50, -50, 60, -60}) {
                curveControl.put(e, controlPoint(e, bend));
                if (minNodeDistance(e) >= 22) break;
            }
        }
    }

    private void placeLabels() {
        List<double[]> taken = new ArrayList<>();
        for (Node n : city.getNodes()) {
            double x = sx(n), y = sy(n) - FLY_H;
            taken.add(new double[]{x - 7, y - 7, 14, 14});
            taken.add(new double[]{x - 16, y - 24, 32, 14});
        }
        for (Edge e : city.getAllEdges()) {
            double[] spot = findLabelSpot(e, taken);
            labelPos.put(e, spot != null ? spot : pointOn(e, 0.5));
        }
    }

    private double[] findLabelSpot(Edge e, List<double[]> taken) {
        for (double t : new double[]{0.5, 0.4, 0.6, 0.3, 0.7, 0.35, 0.65, 0.25, 0.75}) {
            double[] p = pointOn(e, t);
            double[] q = pointOn(e, Math.min(1, t + 0.02));
            double dx = q[0] - p[0], dy = q[1] - p[1];
            double len = Math.hypot(dx, dy);
            double nx = -dy / len, ny = dx / len;
            for (double off : new double[]{0, -16, 16, -26, 26}) {
                double x = p[0] + nx * off, y = p[1] + ny * off;
                double[] r = {x - 17, y - 8, 34, 16};
                if (!overlapsAny(r, taken)) {
                    taken.add(r);
                    return new double[]{x, y};
                }
            }
        }
        return null;
    }

    private static boolean overlapsAny(double[] r, List<double[]> rects) {
        for (double[] s : rects) {
            if (r[0] < s[0] + s[2] && s[0] < r[0] + r[2] && r[1] < s[1] + s[3] && s[1] < r[1] + r[3]) {
                return true;
            }
        }
        return false;
    }

    // =====================================================================
    // Dibujo principal (el orden importa: lo de atrás primero)
    // =====================================================================

    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(SAND);
        gc.fillRect(0, 0, getWidth(), getHeight());

        double scale = Math.min(getWidth() / DESIGN_W, getHeight() / DESIGN_H);
        gc.save();
        gc.scale(scale, scale);

        drawOutskirts(gc);          // afueras: árboles secos, bus y contenedores
        drawGround(gc);             // terrenos y aceras
        drawStreets(gc);            // avenidas, carriles y pasos peatonales
        drawWalls(gc, true);        // muros del fondo
        drawCorridorShadows(gc);
        drawObjects(gc);            // edificios, nodos, carros y decoración
        drawWalls(gc, false);       // muros del frente
        drawFog(gc);                // niebla en los bordes
        drawCorridors(gc);
        if (showCosts) {
            drawCostLabels(gc);
        }
        drawNodeMarkers(gc);
        drawLegend(gc);

        gc.restore();
    }

    // =====================================================================
    // Suelo, calles y muros
    // =====================================================================

    private void drawGround(GraphicsContext gc) {
        Random rnd = new Random(42);
        for (int gx = MIN_X; gx <= MAX_X; gx++) {
            for (int gy = MIN_Y; gy <= MAX_Y; gy++) {
                Color c = DIRT[rnd.nextInt(DIRT.length)];
                if (isSidewalk(gx, gy) || nodeAt.containsKey(key(gx, gy))) {
                    c = SIDEWALK;
                }
                drawDiamond(gc, px(gx, gy), py(gx, gy, 0), IsoProjection.TILE_W, IsoProjection.TILE_H,
                        c, Color.rgb(0, 0, 0, 0.18));
            }
        }
    }

    private void drawStreets(GraphicsContext gc) {
        for (int gx = MIN_X; gx <= MAX_X; gx++) {
            for (int gy = MIN_Y; gy <= MAX_Y; gy++) {
                if (!isStreet(gx, gy)) continue;
                drawDiamond(gc, px(gx, gy), py(gx, gy, 0), IsoProjection.TILE_W, IsoProjection.TILE_H,
                        ASPHALT, Color.rgb(0, 0, 0, 0.25));

                gc.setStroke(LANE);
                if (isIntersection(gx, gy)) {
                    // Paso peatonal: rayas blancas en los 4 bordes del cruce
                    gc.setLineWidth(3);
                    for (double k = -0.3; k <= 0.31; k += 0.2) {
                        gc.strokeLine(px(gx + k, gy - 0.47), py(gx + k, gy - 0.47, 0), px(gx + k, gy - 0.25), py(gx + k, gy - 0.25, 0));
                        gc.strokeLine(px(gx + k, gy + 0.25), py(gx + k, gy + 0.25, 0), px(gx + k, gy + 0.47), py(gx + k, gy + 0.47, 0));
                        gc.strokeLine(px(gx - 0.47, gy + k), py(gx - 0.47, gy + k, 0), px(gx - 0.25, gy + k), py(gx - 0.25, gy + k, 0));
                        gc.strokeLine(px(gx + 0.25, gy + k), py(gx + 0.25, gy + k, 0), px(gx + 0.47, gy + k), py(gx + 0.47, gy + k, 0));
                    }
                } else {
                    // Línea discontinua del carril
                    gc.setLineWidth(1.5);
                    boolean alongX = gy == 3 || gy == 8;
                    if (alongX) {
                        gc.strokeLine(px(gx - 0.25, gy), py(gx - 0.25, gy, 0), px(gx + 0.25, gy), py(gx + 0.25, gy, 0));
                    } else {
                        gc.strokeLine(px(gx, gy - 0.25), py(gx, gy - 0.25, 0), px(gx, gy + 0.25), py(gx, gy + 0.25, 0));
                    }
                }
            }
        }
    }

    /** Muro con franjas de peligro alrededor de la ciudad. back = true dibuja los dos lados del fondo. */
    private void drawWalls(GraphicsContext gc, boolean back) {
        double x0 = MIN_X - 0.5, x1 = MAX_X + 0.5;
        double y0 = MIN_Y - 0.5, y1 = MAX_Y + 0.5;
        if (back) {
            for (int gy = MIN_Y; gy <= MAX_Y; gy++) {         // lado izquierdo del fondo
                drawWallSegment(gc, x0, gy - 0.5, x0, gy + 0.5, isStreet(MIN_X, gy));
            }
            for (int gx = MIN_X; gx <= MAX_X; gx++) {         // lado derecho del fondo
                drawWallSegment(gc, gx - 0.5, y0, gx + 0.5, y0, isStreet(gx, MIN_Y));
            }
        } else {
            for (int gy = MIN_Y; gy <= MAX_Y; gy++) {         // lado derecho del frente
                drawWallSegment(gc, x1, gy - 0.5, x1, gy + 0.5, isStreet(MAX_X, gy));
            }
            for (int gx = MIN_X; gx <= MAX_X; gx++) {         // lado izquierdo del frente
                drawWallSegment(gc, gx - 0.5, y1, gx + 0.5, y1, isStreet(gx, MAX_Y));
            }
        }
    }

    /** Un tramo de muro entre dos puntos de la cuadrícula. Si es una salida de calle, se dibuja una barrera. */
    private void drawWallSegment(GraphicsContext gc, double ax, double ay, double bx, double by, boolean gate) {
        double h = gate ? 8 : WALL_H;
        double[] xs = {px(ax, ay), px(bx, by), px(bx, by), px(ax, ay)};
        double[] ys = {py(ax, ay, 0), py(bx, by, 0), py(bx, by, h), py(ax, ay, h)};
        gc.setFill(gate ? Color.web("#5a5a56") : WALL);
        gc.fillPolygon(xs, ys, 4);
        gc.setStroke(OUTLINE);
        gc.setLineWidth(1);
        gc.strokePolygon(xs, ys, 4);

        // Franjas amarillas y negras en la parte de arriba
        int stripes = gate ? 6 : 4;
        for (int i = 0; i < stripes; i++) {
            double u1 = (double) i / stripes, u2 = (double) (i + 1) / stripes;
            double sx1 = ax + (bx - ax) * u1, sy1 = ay + (by - ay) * u1;
            double sx2 = ax + (bx - ax) * u2, sy2 = ay + (by - ay) * u2;
            double band = gate ? h : 4;
            gc.setFill(i % 2 == 0 ? HAZARD_Y : HAZARD_K);
            gc.fillPolygon(
                    new double[]{px(sx1, sy1), px(sx2, sy2), px(sx2, sy2), px(sx1, sy1)},
                    new double[]{py(sx1, sy1, h - band), py(sx2, sy2, h - band), py(sx2, sy2, h), py(sx1, sy1, h)}, 4);
        }
        // Poste en el extremo del tramo
        gc.setStroke(WALL_SIDE);
        gc.setLineWidth(3);
        gc.strokeLine(px(ax, ay), py(ax, ay, 0), px(ax, ay), py(ax, ay, h + 4));
    }

    // =====================================================================
    // Objetos: de atrás (x + y pequeño) hacia adelante (x + y grande)
    // =====================================================================

    private void drawObjects(GraphicsContext gc) {
        for (int sum = MIN_X + MIN_Y; sum <= MAX_X + MAX_Y; sum++) {
            for (int gx = MIN_X; gx <= MAX_X; gx++) {
                int gy = sum - gx;
                if (gy < MIN_Y || gy > MAX_Y) continue;
                String k = key(gx, gy);

                Node node = nodeAt.get(k);
                if (node != null) {
                    drawNode(gc, node, gx, gy);
                } else if (cars.containsKey(k)) {
                    drawCar(gc, gx, gy, cars.get(k));
                } else if (props.containsKey(k)) {
                    drawProp(gc, props.get(k), gx, gy);
                }
            }
        }
    }

    private void drawNode(GraphicsContext gc, Node node, int gx, int gy) {
        switch (node.getType()) {
            case CONTROL_CENTER -> drawControlCenter(gc, gx, gy);
            case SHELTER -> drawShelter(gc, gx, gy);
            case HOSPITAL -> drawHospital(gc, gx, gy);
            case TOWER -> drawTower(gc, gx, gy);
            case HORDE_SPAWN -> drawHordeSpawn(gc, gx, gy);
        }
    }

    private void drawProp(GraphicsContext gc, Prop p, int gx, int gy) {
        switch (p.kind()) {
            case BUILDING -> drawBuilding(gc, gx, gy, p.variant(), p.height());
            case CONTAINERS -> drawContainers(gc, gx, gy, p.variant());
            case TREE -> drawDeadTree(gc, px(gx, gy), py(gx, gy, 0), p.variant());
            case BARRELS -> drawBarrels(gc, gx, gy, p.variant());
            case RUBBLE -> drawRubble(gc, gx, gy, p.variant());
        }
    }

    // =====================================================================
    // Figura base: prisma isométrico (caja) apoyado en la cuadrícula
    // =====================================================================

    /**
     * Caja con base rectangular centrada en (gx, gy).
     * a = mitad del largo en X, b = mitad del largo en Y (en casillas),
     * z0 = altura donde empieza, h = altura de la caja (en píxeles).
     */
    private void drawPrism(GraphicsContext gc, double gx, double gy, double a, double b,
                           double z0, double h, Color top, Color left, Color right) {
        double lx = px(gx - a, gy + b), fx = px(gx + a, gy + b), rx = px(gx + a, gy - b), bx = px(gx - a, gy - b);
        double ly = py(gx - a, gy + b, 0), fy = py(gx + a, gy + b, 0), ry = py(gx + a, gy - b, 0), by = py(gx - a, gy - b, 0);
        double z1 = z0 + h;

        double[] leftX = {lx, fx, fx, lx};
        double[] leftY = {ly - z0, fy - z0, fy - z1, ly - z1};
        double[] rightX = {fx, rx, rx, fx};
        double[] rightY = {fy - z0, ry - z0, ry - z1, fy - z1};
        double[] topX = {bx, rx, fx, lx};
        double[] topY = {by - z1, ry - z1, fy - z1, ly - z1};

        gc.setStroke(OUTLINE);
        gc.setLineWidth(1);
        gc.setFill(left);
        gc.fillPolygon(leftX, leftY, 4);
        gc.strokePolygon(leftX, leftY, 4);
        gc.setFill(right);
        gc.fillPolygon(rightX, rightY, 4);
        gc.strokePolygon(rightX, rightY, 4);
        gc.setFill(top);
        gc.fillPolygon(topX, topY, 4);
        gc.strokePolygon(topX, topY, 4);
    }

    /**
     * Pinta un rectángulo (ventana, letrero…) sobre una cara de una caja.
     * leftFace = true usa la cara izquierda; u1..u2 = posición horizontal (0 a 1); z1..z2 = alturas.
     */
    private void drawOnFace(GraphicsContext gc, double gx, double gy, double a, double b, boolean leftFace,
                            double u1, double u2, double z1, double z2, Color color) {
        double ax, ay, bx, by;
        if (leftFace) {          // de la esquina izquierda a la del frente
            ax = gx - a; ay = gy + b; bx = gx + a; by = gy + b;
        } else {                 // de la esquina del frente a la derecha
            ax = gx + a; ay = gy + b; bx = gx + a; by = gy - b;
        }
        double x1 = ax + (bx - ax) * u1, y1 = ay + (by - ay) * u1;
        double x2 = ax + (bx - ax) * u2, y2 = ay + (by - ay) * u2;
        gc.setFill(color);
        gc.fillPolygon(
                new double[]{px(x1, y1), px(x2, y2), px(x2, y2), px(x1, y1)},
                new double[]{py(x1, y1, z1), py(x2, y2, z1), py(x2, y2, z2), py(x1, y1, z2)}, 4);
    }

    /** Filas de ventanas en las dos caras visibles. litChance = probabilidad de ventana encendida. */
    private void drawWindows(GraphicsContext gc, double gx, double gy, double a, double b, double h,
                             int seed, double litChance, Color dark) {
        Random rnd = new Random(seed);
        int cols = 3;
        for (double z = 7; z + 6 < h - 2; z += 11) {
            for (boolean left : new boolean[]{true, false}) {
                for (int c = 0; c < cols; c++) {
                    double u1 = (c + 0.25) / cols, u2 = (c + 0.7) / cols;
                    double r = rnd.nextDouble();
                    Color col = r < litChance ? Color.web("#f2c94c") : (r < litChance + 0.15 ? Color.web("#1b1b1b") : dark);
                    drawOnFace(gc, gx, gy, a, b, left, u1, u2, z, z + 6, col);
                }
            }
        }
    }

    // =====================================================================
    // Decoración
    // =====================================================================

    /** Edificio abandonado con ventanas, toldo y objetos en el techo según su variante. */
    private void drawBuilding(GraphicsContext gc, int gx, int gy, int variant, int h) {
        Color[] c = BUILDING_COLORS[variant % BUILDING_COLORS.length];
        double a = 0.30 + (variant % 3) * 0.04;
        double b = 0.30 + (variant % 2) * 0.05;
        drawPrism(gc, gx, gy, a, b, 0, h, c[0], c[1], c[2]);
        drawWindows(gc, gx, gy, a, b, h, variant, 0.04, Color.web("#2f3438"));

        // Toldo de rayas en la planta baja (1 de cada 4 edificios)
        if (variant % 4 == 0) {
            for (int i = 0; i < 6; i++) {
                drawOnFace(gc, gx, gy, a, b, true, i / 6.0, (i + 1) / 6.0, 5, 9,
                        i % 2 == 0 ? Color.web("#b9473b") : Color.web("#e8dfc9"));
            }
        }
        // Objetos en el techo
        double topX = px(gx, gy), topY = py(gx, gy, h);
        switch (variant % 3) {
            case 0 -> drawWaterTank(gc, topX, topY);
            case 1 -> drawPrism(gc, gx + 0.08, gy - 0.05, 0.08, 0.06, h, 5,
                    Color.web("#b7b7ae"), Color.web("#9a9a92"), Color.web("#7d7d76"));   // aire acondicionado
            default -> {
                gc.setStroke(Color.web("#5d6266"));                                     // antena
                gc.setLineWidth(1.5);
                gc.strokeLine(topX + 4, topY, topX + 4, topY - 14);
                gc.strokeLine(topX, topY - 10, topX + 8, topY - 10);
            }
        }
        // Grieta
        gc.setStroke(Color.rgb(20, 20, 20, 0.6));
        gc.setLineWidth(1.2);
        double cx = px(gx + a, gy + b * 0.2), cy = py(gx + a, gy + b * 0.2, h * 0.7);
        gc.strokeLine(cx, cy, cx + 3, cy + 7);
        gc.strokeLine(cx + 3, cy + 7, cx + 1, cy + 13);
    }

    private void drawWaterTank(GraphicsContext gc, double x, double y) {
        gc.setStroke(Color.web("#4a4a46"));
        gc.setLineWidth(1.2);
        gc.strokeLine(x - 4, y - 2, x - 4, y - 7);
        gc.strokeLine(x + 4, y - 2, x + 4, y - 7);
        gc.setFill(Color.web("#5f8790"));
        gc.fillRect(x - 6, y - 17, 12, 10);
        gc.setFill(Color.web("#79a3ab"));
        gc.fillOval(x - 6, y - 20, 12, 6);
        gc.setStroke(OUTLINE);
        gc.setLineWidth(1);
        gc.strokeRect(x - 6, y - 17, 12, 10);
        gc.strokeOval(x - 6, y - 20, 12, 6);
    }

    /** Dos o tres contenedores de colores, con estrías. */
    private void drawContainers(GraphicsContext gc, int gx, int gy, int variant) {
        boolean alongX = variant % 2 == 0;
        double a = alongX ? 0.38 : 0.14, b = alongX ? 0.14 : 0.38;
        double ox = alongX ? 0 : 0.17, oy = alongX ? 0.17 : 0;
        Color[] c1 = CONTAINER_COLORS[variant % CONTAINER_COLORS.length];
        Color[] c2 = CONTAINER_COLORS[(variant / 4) % CONTAINER_COLORS.length];
        drawContainer(gc, gx - ox, gy - oy, a, b, 0, c1);
        drawContainer(gc, gx + ox, gy + oy, a, b, 0, c2);
        if (variant % 3 == 0) {
            drawContainer(gc, gx - ox, gy - oy, a, b, 11, CONTAINER_COLORS[(variant / 16) % CONTAINER_COLORS.length]);
        }
    }

    private void drawContainer(GraphicsContext gc, double gx, double gy, double a, double b, double z0, Color[] c) {
        drawPrism(gc, gx, gy, a, b, z0, 11, c[0], c[1], c[2]);
        for (int i = 1; i < 8; i++) {   // estrías verticales
            drawOnFace(gc, gx, gy, a, b, true, i / 8.0, i / 8.0 + 0.02, z0 + 1, z0 + 10, c[2]);
        }
    }

    /** Árbol seco: tronco y ramas sin hojas. */
    private void drawDeadTree(GraphicsContext gc, double x, double y, int variant) {
        double s = 0.8 + (variant % 5) * 0.1;
        double lean = (variant % 3 - 1) * 2;
        gc.setFill(Color.rgb(0, 0, 0, 0.18));
        gc.fillOval(x - 9, y - 3, 18, 6);
        gc.setStroke(TRUNK);
        gc.setLineWidth(3);
        gc.strokeLine(x, y, x + lean, y - 22 * s);
        gc.setLineWidth(1.8);
        gc.strokeLine(x + lean * 0.5, y - 12 * s, x - 9 * s, y - 20 * s);
        gc.strokeLine(x + lean * 0.7, y - 16 * s, x + 8 * s, y - 27 * s);
        gc.strokeLine(x + lean, y - 22 * s, x - 3 * s, y - 32 * s);
        gc.setLineWidth(1);
        gc.strokeLine(x - 9 * s, y - 20 * s, x - 13 * s, y - 24 * s);
        gc.strokeLine(x + 8 * s, y - 27 * s, x + 12 * s, y - 29 * s);
        gc.strokeLine(x - 3 * s, y - 32 * s, x + 1 * s, y - 36 * s);
    }

    /** Grupo de barriles oxidados. */
    private void drawBarrels(GraphicsContext gc, int gx, int gy, int variant) {
        Color[] bodies = {Color.web("#7a4b2e"), Color.web("#3f6e9a"), Color.web("#4f7d4f")};
        double[][] spots = {{-0.15, -0.1}, {0.12, -0.05}, {0, 0.15}};
        for (int i = 0; i < 3; i++) {
            double x = px(gx + spots[i][0], gy + spots[i][1]);
            double y = py(gx + spots[i][0], gy + spots[i][1], 0);
            Color body = bodies[(variant + i) % bodies.length];
            gc.setFill(body);
            gc.fillRect(x - 4, y - 10, 8, 10);
            gc.setFill(body.brighter());
            gc.fillOval(x - 4, y - 12, 8, 4);
            gc.setStroke(OUTLINE);
            gc.setLineWidth(0.8);
            gc.strokeRect(x - 4, y - 10, 8, 10);
            gc.strokeLine(x - 4, y - 5, x + 4, y - 5);
        }
    }

    /** Escombros: losas y piedras sueltas. */
    private void drawRubble(GraphicsContext gc, int gx, int gy, int variant) {
        Random rnd = new Random(variant);
        drawPrism(gc, gx - 0.1, gy + 0.05, 0.22, 0.12, 0, 4,
                Color.web("#a9a79f"), Color.web("#8d8b84"), Color.web("#75736d"));
        for (int i = 0; i < 6; i++) {
            double dx = rnd.nextDouble() * 0.7 - 0.35, dy = rnd.nextDouble() * 0.7 - 0.35;
            double x = px(gx + dx, gy + dy), y = py(gx + dx, gy + dy, 0);
            gc.setFill(i % 2 == 0 ? Color.web("#8a877e") : Color.web("#6e6b64"));
            gc.fillOval(x - 3, y - 2, 6, 4);
        }
    }

    /** Carro abandonado orientado según la calle. */
    private void drawCar(GraphicsContext gc, int gx, int gy, int variant) {
        boolean alongX = gy == 3 || gy == 8;
        double lane = variant % 2 == 0 ? 0.18 : -0.18;
        double cx = alongX ? gx : gx + lane;
        double cy = alongX ? gy + lane : gy;
        double a = alongX ? 0.28 : 0.13, b = alongX ? 0.13 : 0.28;
        Color body = CAR_COLORS[variant % CAR_COLORS.length];
        drawPrism(gc, cx, cy, a, b, 0, 5, body.brighter(), body, body.darker());
        drawPrism(gc, cx, cy, a * 0.55, b * 0.8, 5, 4,
                Color.web("#2b3036"), body.darker(), body.darker().darker());
    }

    // =====================================================================
    // Ubicaciones del grafo (nodos)
    // =====================================================================

    /** Refugio: casa de ladrillo con ventanas encendidas, cajas en el techo y bandera verde. */
    private void drawShelter(GraphicsContext gc, int gx, int gy) {
        double a = 0.36, b = 0.36, h = 30;
        drawPrism(gc, gx, gy, a, b, 0, h, Color.web("#b07c55"), Color.web("#9c6b48"), Color.web("#7e5538"));
        drawWindows(gc, gx, gy, a, b, h, gx * 31 + gy, 0.7, Color.web("#3a2f27"));
        // Sacos de arena delante
        for (int i = 0; i < 5; i++) {
            double t = -0.35 + i * 0.17;
            double x = px(gx + t, gy + 0.47), y = py(gx + t, gy + 0.47, 0);
            gc.setFill(Color.web("#b9a77d"));
            gc.fillOval(x - 5, y - 5, 10, 5);
            gc.setStroke(Color.web("#7d6f50"));
            gc.setLineWidth(0.8);
            gc.strokeOval(x - 5, y - 5, 10, 5);
        }
        // Cajas de suministros en el techo
        drawPrism(gc, gx - 0.12, gy + 0.05, 0.08, 0.08, h, 6,
                Color.web("#c49a5c"), Color.web("#a8814a"), Color.web("#8b6a3c"));
        // Bandera verde: hay supervivientes
        double x = px(gx + 0.1, gy - 0.1), y = py(gx + 0.1, gy - 0.1, h);
        gc.setStroke(Color.web("#d0d0d0"));
        gc.setLineWidth(1.5);
        gc.strokeLine(x, y, x, y - 22);
        gc.setFill(Color.web("#3fbf5f"));
        gc.fillPolygon(new double[]{x, x + 12, x}, new double[]{y - 22, y - 18, y - 14}, 3);
    }

    /** Hospital abandonado: edificio blanco, ventanas rotas y cruz roja en el techo. */
    private void drawHospital(GraphicsContext gc, int gx, int gy) {
        double a = 0.42, b = 0.42, h = 48;
        drawPrism(gc, gx, gy, a, b, 0, h, Color.web("#dedbd2"), Color.web("#c3c0b6"), Color.web("#a6a399"));
        drawWindows(gc, gx, gy, a, b, h, gx * 17 + gy, 0.0, Color.web("#41505a"));
        // Franja roja horizontal
        drawOnFace(gc, gx, gy, a, b, true, 0, 1, h - 6, h - 3, Color.web("#b9473b"));
        drawOnFace(gc, gx, gy, a, b, false, 0, 1, h - 6, h - 3, Color.web("#a03d33"));
        // Cruz roja pintada en el techo (en perspectiva)
        drawRoofRect(gc, gx, gy, 0.26, 0.26, h, Color.web("#e9e6de"));   // placa blanca
        double x = px(gx, gy), y = py(gx, gy, h);                           // cruz roja
        gc.setFill(Color.web("#c0392b"));
        gc.fillRect(x - 9, y - 2.5, 18, 5);
        gc.fillRect(x - 2.5, y - 7, 5, 14);
    }

    /** Rectángulo plano pintado sobre un techo a la altura z (a, b = medio largo en X y en Y). */
    private void drawRoofRect(GraphicsContext gc, double gx, double gy, double a, double b, double z, Color color) {
        gc.setFill(color);
        gc.fillPolygon(
                new double[]{px(gx - a, gy - b), px(gx + a, gy - b), px(gx + a, gy + b), px(gx - a, gy + b)},
                new double[]{py(gx - a, gy - b, z), py(gx + a, gy - b, z), py(gx + a, gy + b, z), py(gx - a, gy + b, z)}, 4);
    }

    /** Torre de control: estructura metálica de celosía con cabina, antena y luz roja. */
    private void drawTower(GraphicsContext gc, int gx, int gy) {
        double top = 80, base = 0.2, upper = 0.09;
        gc.setStroke(Color.web("#4a4f53"));
        gc.setLineWidth(2);
        double[][] corners = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
        for (double[] c : corners) {   // 4 patas que se juntan hacia arriba
            gc.strokeLine(px(gx + c[0] * base, gy + c[1] * base), py(gx + c[0] * base, gy + c[1] * base, 0),
                    px(gx + c[0] * upper, gy + c[1] * upper), py(gx + c[0] * upper, gy + c[1] * upper, top));
        }
        // Refuerzos en X en las dos caras visibles
        gc.setLineWidth(1);
        for (int level = 0; level < 4; level++) {
            double z1 = level * 20, z2 = z1 + 20;
            double s1 = base + (upper - base) * (z1 / top), s2 = base + (upper - base) * (z2 / top);
            gc.strokeLine(px(gx - s1, gy + s1), py(gx - s1, gy + s1, z1), px(gx + s2, gy + s2), py(gx + s2, gy + s2, z2));
            gc.strokeLine(px(gx + s1, gy + s1), py(gx + s1, gy + s1, z1), px(gx - s2, gy + s2), py(gx - s2, gy + s2, z2));
            gc.strokeLine(px(gx + s1, gy + s1), py(gx + s1, gy + s1, z1), px(gx + s2, gy - s2), py(gx + s2, gy - s2, z2));
            gc.strokeLine(px(gx + s1, gy - s1), py(gx + s1, gy - s1, z1), px(gx + s2, gy + s2), py(gx + s2, gy + s2, z2));
        }
        // Cabina
        drawPrism(gc, gx, gy, 0.15, 0.15, top, 10, Color.web("#7b8186"), Color.web("#5d6266"), Color.web("#4a4f53"));
        drawOnFace(gc, gx, gy, 0.15, 0.15, true, 0.15, 0.85, top + 3, top + 7, Color.web("#a8d0e0"));
        drawOnFace(gc, gx, gy, 0.15, 0.15, false, 0.15, 0.85, top + 3, top + 7, Color.web("#86b0c0"));
        // Antena y luz roja
        double x = px(gx, gy), y = py(gx, gy, top + 10);
        gc.setStroke(Color.web("#5d6266"));
        gc.setLineWidth(1.5);
        gc.strokeLine(x, y, x, y - 18);
        gc.setFill(Color.web("#ff3b30"));
        gc.fillOval(x - 3.5, y - 24, 7, 7);
    }

    /** Centro de control: búnker militar con helipuerto, sacos de arena y bandera. */
    private void drawControlCenter(GraphicsContext gc, int gx, int gy) {
        double a = 0.45, b = 0.45, h = 18;
        drawPrism(gc, gx, gy, a, b, 0, h, Color.web("#5d6630"), Color.web("#4b5320"), Color.web("#3b4219"));
        drawOnFace(gc, gx, gy, a, b, true, 0.35, 0.65, 0, 11, Color.web("#2a2e14"));   // puerta
        // Helipuerto en el techo (círculo en perspectiva con una H)
        double x = px(gx, gy), y = py(gx, gy, h);
        gc.setStroke(Color.web("#f2c94c"));
        gc.setLineWidth(2);
        gc.strokeOval(x - 17, y - 8.5, 34, 17);
        gc.setStroke(Color.web("#f2c94c"));
        gc.setLineWidth(2);
        gc.strokeLine(x - 5, y - 4, x - 5, y + 4);
        gc.strokeLine(x + 5, y - 4, x + 5, y + 4);
        gc.strokeLine(x - 5, y, x + 5, y);
    }

    /** Punto de aparición de hordas: charco de sangre y siluetas de zombis. */
    private void drawHordeSpawn(GraphicsContext gc, int gx, int gy) {
        drawDiamond(gc, px(gx, gy), py(gx, gy, 0), IsoProjection.TILE_W * 0.95, IsoProjection.TILE_H * 0.95,
                Color.web("#6b2020"), null);
        drawDiamond(gc, px(gx + 0.05, gy), py(gx + 0.05, gy, 0), IsoProjection.TILE_W * 0.55, IsoProjection.TILE_H * 0.55,
                Color.web("#8a2626"), null);
        double[][] spots = {{-0.2, -0.1}, {0.05, -0.25}, {0.2, 0.05}, {-0.05, 0.2}};
        for (double[] s : spots) {
            double x = px(gx + s[0], gy + s[1]), y = py(gx + s[0], gy + s[1], 0);
            gc.setFill(Color.web("#1d1412"));
            gc.fillOval(x - 2.5, y - 11, 5, 10);     // cuerpo
            gc.fillOval(x - 2, y - 15, 4, 4);        // cabeza
            gc.setStroke(Color.web("#1d1412"));
            gc.setLineWidth(1.2);
            gc.strokeLine(x + 1, y - 9, x + 5, y - 10);   // brazo estirado
        }
    }

    // =====================================================================
    // Afueras y niebla
    // =====================================================================

    /** Afueras: árboles secos, un bus abandonado y contenedores fuera del muro. */
    private void drawOutskirts(GraphicsContext gc) {
        double[][] trees = {{6, -3}, {9, -4}, {12, -3}, {3, -3}, {18, 4}, {19, 7}, {18, 10},
                {-3, 2}, {-2, 9}, {4, 15}, {8, 15}, {11, 15}, {7, 16}};
        int v = 0;
        for (double[] t : trees) {
            drawDeadTree(gc, px(t[0], t[1]), py(t[0], t[1], 0), v++ * 7);
        }
        // Bus abandonado (afuera, arriba a la derecha)
        drawPrism(gc, 14.5, -2.8, 0.7, 0.22, 0, 14,
                Color.web("#c9c3b4"), Color.web("#a8423a"), Color.web("#86342d"));
        for (int i = 0; i < 6; i++) {
            drawOnFace(gc, 14.5, -2.8, 0.7, 0.22, true, 0.08 + i * 0.15, 0.18 + i * 0.15, 7, 12, Color.web("#2f3438"));
        }
        // Contenedores (afuera, abajo a la derecha)
        drawContainer(gc, 18.6, 1.5, 0.14, 0.38, 0, CONTAINER_COLORS[1]);
        drawContainer(gc, 18.9, 1.5, 0.14, 0.38, 0, CONTAINER_COLORS[0]);
        drawContainer(gc, 18.6, 1.5, 0.14, 0.38, 11, CONTAINER_COLORS[2]);
    }

    /** Niebla: nubes circulares que se desvanecen (degradado radial) en los bordes del mapa. */
    private void drawFog(GraphicsContext gc) {
        double[][] clouds = {          // {centro X, centro Y, radio}
                {120, 60, 260}, {420, -40, 220}, {820, 20, 260}, {1080, 180, 230},
                {1060, 520, 280}, {760, 720, 260}, {380, 700, 220}, {-20, 420, 240}
        };
        for (double[] c : clouds) {
            RadialGradient fog = new RadialGradient(0, 0, c[0], c[1], c[2], false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(240, 237, 230, 0.75)),
                    new Stop(0.55, Color.rgb(240, 237, 230, 0.35)),
                    new Stop(1, Color.rgb(240, 237, 230, 0)));
            gc.setFill(fog);
            gc.fillOval(c[0] - c[2], c[1] - c[2], c[2] * 2, c[2] * 2);
        }
    }

    // =====================================================================
    // Grafo: corredores, costos y nodos
    // =====================================================================

    private void drawCorridorShadows(GraphicsContext gc) {
        gc.setStroke(Color.rgb(0, 0, 0, 0.22));
        gc.setLineWidth(2);
        for (Edge e : city.getAllEdges()) {
            gc.strokeLine(sx(e.getA()), sy(e.getA()), sx(e.getB()), sy(e.getB()));
        }
    }

    private void strokeCorridor(GraphicsContext gc, Edge e) {
        double x1 = sx(e.getA()), y1 = sy(e.getA()) - FLY_H;
        double x2 = sx(e.getB()), y2 = sy(e.getB()) - FLY_H;
        double[] c = curveControl.get(e);
        gc.beginPath();
        gc.moveTo(x1, y1);
        if (c == null) {
            gc.lineTo(x2, y2);
        } else {
            gc.quadraticCurveTo(c[0], c[1], x2, y2);
        }
        gc.stroke();
    }

    private void drawCorridors(GraphicsContext gc) {
        // Haz vertical punteado de cada nodo hasta su marcador
        gc.setStroke(Color.rgb(255, 255, 255, 0.55));
        gc.setLineWidth(1);
        gc.setLineDashes(3, 3);
        for (Node n : city.getNodes()) {
            gc.strokeLine(sx(n), sy(n) - 10, sx(n), sy(n) - FLY_H);
        }
        gc.setLineDashes(null);

        for (Edge e : city.getAllEdges()) {
            gc.setStroke(Color.rgb(0, 0, 0, 0.75));
            gc.setLineWidth(5);
            strokeCorridor(gc, e);
            gc.setStroke(dangerColor(e));
            gc.setLineWidth(2.5);
            strokeCorridor(gc, e);
        }
    }

    private void drawCostLabels(GraphicsContext gc) {
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
        for (Edge e : city.getAllEdges()) {
            double[] p = labelPos.get(e);
            String text = String.format("%.1f", e.getCost());
            double w = text.length() * 6.5 + 6;
            gc.setFill(Color.rgb(15, 15, 15, 0.85));
            gc.fillRect(p[0] - w / 2, p[1] - 7, w, 14);
            gc.setStroke(dangerColor(e));
            gc.setLineWidth(1);
            gc.strokeRect(p[0] - w / 2, p[1] - 7, w, 14);
            gc.setFill(Color.WHITE);
            gc.fillText(text, p[0] - w / 2 + 3, p[1] + 4);
        }
    }

    private void drawNodeMarkers(GraphicsContext gc) {
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        for (Node n : city.getNodes()) {
            double x = sx(n), y = sy(n) - FLY_H;
            gc.setFill(typeColor(n));
            gc.fillOval(x - 6, y - 6, 12, 12);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.5);
            gc.strokeOval(x - 6, y - 6, 12, 12);

            gc.setFill(Color.rgb(15, 15, 15, 0.85));
            gc.fillRect(x - 16, y - 24, 32, 14);
            gc.setFill(Color.web("#f0e8c8"));
            gc.fillText(n.getId(), x - 14, y - 13);
        }
    }

    private static Color dangerColor(Edge e) {
        double factor = e.getCost() / e.getDistance();
        double t = Math.max(0, Math.min(1, (factor - 1) / 1.2));
        if (t < 0.5) {
            return SAFE.interpolate(MEDIUM, t * 2);
        }
        return MEDIUM.interpolate(DANGER, (t - 0.5) * 2);
    }

    private static Color typeColor(Node n) {
        return switch (n.getType()) {
            case CONTROL_CENTER -> Color.web("#9acd32");
            case SHELTER -> Color.web("#e0a060");
            case HOSPITAL -> Color.web("#f0f0f0");
            case TOWER -> Color.web("#8fa3b8");
            case HORDE_SPAWN -> Color.web("#e53935");
            default -> Color.WHITE;
        };
    }

    // =====================================================================
    // Leyendas (sobre paneles oscuros para que se lean encima de la arena)
    // =====================================================================

    private void drawLegend(GraphicsContext gc) {
        // Panel superior: título y colores de los corredores
        gc.setFill(Color.rgb(20, 20, 18, 0.82));
        gc.fillRoundRect(10, 12, 235, 142, 12, 12);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 22));
        gc.setFill(Color.web("#e2b93b"));
        gc.fillText("PATHFINDEAD", 20, 40);

        gc.setFont(Font.font("Monospaced", 12));
        String[] levels = {"Corredor seguro", "Peligro medio", "Peligro alto"};
        Color[] levelColors = {SAFE, MEDIUM, DANGER};
        for (int i = 0; i < levels.length; i++) {
            double y = 64 + i * 22;
            gc.setStroke(levelColors[i]);
            gc.setLineWidth(3);
            gc.strokeLine(20, y, 44, y);
            label(gc, levels[i], 52, y + 4);
        }
        gc.setFill(Color.rgb(0, 0, 0, 0.9));
        gc.fillRect(20, 125, 28, 14);
        gc.setStroke(Color.web("#888888"));
        gc.setLineWidth(1);
        gc.strokeRect(20, 125, 28, 14);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
        label(gc, "8.2", 24, 136);
        gc.setFont(Font.font("Monospaced", 12));
        label(gc, "Costo del corredor", 56, 136);

        // Panel inferior: tipos de ubicación
        gc.setFill(Color.rgb(20, 20, 18, 0.82));
        gc.fillRoundRect(10, 540, 205, 146, 12, 12);
        String[] names = {"Centro de control", "Refugio", "Hospital abandonado",
                "Torre de control", "Aparición de horda"};
        Color[] colors = {Color.web("#9acd32"), Color.web("#e0a060"), Color.web("#f0f0f0"),
                Color.web("#8fa3b8"), Color.web("#e53935")};
        for (int i = 0; i < names.length; i++) {
            double y = 562 + i * 24;
            gc.setFill(colors[i]);
            gc.fillOval(20, y - 6, 12, 12);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.5);
            gc.strokeOval(20, y - 6, 12, 12);
            label(gc, names[i], 40, y + 4);
        }
    }

    private void label(GraphicsContext gc, String text, double x, double y) {
        gc.setFill(Color.WHITE);
        gc.fillText(text, x, y);
    }

    /** Rombo centrado en (cx, cy). */
    public static void drawDiamond(GraphicsContext gc, double cx, double cy,
                                   double w, double h, Color fill, Color stroke) {
        double[] xs = {cx, cx + w / 2, cx, cx - w / 2};
        double[] ys = {cy - h / 2, cy, cy + h / 2, cy};
        gc.setFill(fill);
        gc.fillPolygon(xs, ys, 4);
        if (stroke != null) {
            gc.setStroke(stroke);
            gc.setLineWidth(1);
            gc.strokePolygon(xs, ys, 4);
        }
    }
}