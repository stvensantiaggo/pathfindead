package pathfindead.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import pathfindead.algorithms.AllPairsResult;
import pathfindead.algorithms.Dijkstra;
import pathfindead.algorithms.FloydWarshall;
import pathfindead.algorithms.Kruskal;
import pathfindead.algorithms.PathResult;
import pathfindead.algorithms.Prim;
import pathfindead.algorithms.SpanningTree;
import pathfindead.model.Edge;
import pathfindead.model.Graph;
import pathfindead.model.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel de controles a la derecha del mapa: origen, destino, algoritmo,
 * botones para ejecutar y limpiar, y el resultado en texto.
 */
public class ControlPanel extends VBox {

    // Nombres de los algoritmos que aparecen en el selector
    private static final String DIJKSTRA = "Dijkstra (ruta más barata)";
    private static final String FLOYD = "Floyd-Warshall (todos los pares)";
    private static final String PRIM = "Prim (árbol mínimo)";
    private static final String KRUSKAL = "Kruskal (árbol mínimo)";

    // Punto 78: cada algoritmo se dibuja con un color distinto
    private static final Color DIJKSTRA_COLOR = Color.web("#00e5ff");   // cian
    private static final Color FLOYD_COLOR = Color.web("#ff4fd8");      // magenta
    private static final Color PRIM_COLOR = Color.web("#c6ff00");       // verde lima
    private static final Color KRUSKAL_COLOR = Color.web("#2979ff");    // azul

    private final Graph city;
    private final MapView map;

    private final ComboBox<Node> origin = new ComboBox<>();
    private final ComboBox<Node> destination = new ComboBox<>();
    private final ComboBox<String> algorithm = new ComboBox<>();
    private final Label resultTitle = new Label("RESULTADO");
    private final Label result = new Label();

    public ControlPanel(Graph city, MapView map) {
        super(10);
        this.city = city;
        this.map = map;

        setPadding(new Insets(16));
        setPrefWidth(280);
        setStyle("-fx-background-color: #1e1e1b;");

        Label title = new Label("CENTRO DE MANDO");
        title.setStyle("-fx-text-fill: #e2b93b; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Origen y destino
        origin.getItems().addAll(city.getNodes());
        origin.setValue(city.getNode("C-01"));
        origin.setMaxWidth(Double.MAX_VALUE);

        destination.getItems().addAll(city.getNodes());
        destination.setValue(city.getNode("Z-02"));
        destination.setMaxWidth(Double.MAX_VALUE);

        // Selector con los 4 algoritmos (puntos 68, 72, 74 y 76)
        algorithm.getItems().addAll(DIJKSTRA, FLOYD, PRIM, KRUSKAL);
        algorithm.setValue(DIJKSTRA);
        algorithm.setMaxWidth(Double.MAX_VALUE);
        algorithm.setOnAction(event -> updateEnabledFields());

        Button run = new Button("Ejecutar");
        run.setStyle("-fx-background-color: #e2b93b; -fx-font-weight: bold;");
        run.setOnAction(event -> runAlgorithm());

        Button clear = new Button("Limpiar");
        clear.setOnAction(event -> {
            map.clearHighlight();
            showResult("RESULTADO", Color.web("#d8d2c0"), "Mapa limpio.");
        });

        CheckBox showCosts = new CheckBox("Mostrar costos");
        showCosts.setSelected(true);
        showCosts.setStyle("-fx-text-fill: #d8d2c0;");
        showCosts.setOnAction(event -> map.setShowCosts(showCosts.isSelected()));

        // Resultado dentro de un panel con barra de desplazamiento (los árboles tienen 13 líneas)
        result.setWrapText(true);
        result.setStyle("-fx-text-fill: #f0e8c8; -fx-font-family: 'Monospaced'; -fx-font-size: 12px;");
        ScrollPane scroll = new ScrollPane(result);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1e1e1b; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);   // ocupa el espacio que sobre abajo

        showResult("RESULTADO", Color.web("#d8d2c0"),
                "Elige origen, destino y algoritmo, y pulsa Ejecutar.");

        getChildren().addAll(
                title,
                label("Origen"), origin,
                label("Destino"), destination,
                label("Algoritmo"), algorithm,
                new HBox(8, run, clear),
                showCosts,
                new Separator(),
                resultTitle, scroll
        );
    }

    /** Etiqueta pequeña con el estilo del panel. */
    private Label label(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #d8d2c0; -fx-font-weight: bold;");
        return l;
    }

    /** Prim y Kruskal no usan destino; Kruskal tampoco usa origen. */
    private void updateEnabledFields() {
        String a = algorithm.getValue();
        destination.setDisable(a.equals(PRIM) || a.equals(KRUSKAL));
        origin.setDisable(a.equals(KRUSKAL));
    }

    /** Muestra el título del resultado con el color del algoritmo y el texto debajo. */
    private void showResult(String title, Color color, String text) {
        resultTitle.setText(title);
        resultTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + toCss(color) + ";");
        result.setText(text);
    }

    private static String toCss(Color c) {
        return String.format("#%02x%02x%02x",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }

    /** Se ejecuta al pulsar "Ejecutar": corre el algoritmo elegido y muestra su resultado. */
    private void runAlgorithm() {
        Node from = origin.getValue();
        Node to = destination.getValue();
        if (from == null || to == null) {
            showResult("RESULTADO", Color.web("#d8d2c0"), "Elige un origen y un destino.");
            return;
        }

        switch (algorithm.getValue()) {
            case DIJKSTRA -> runDijkstra(from, to);
            case FLOYD -> runFloydWarshall(from, to);
            case PRIM -> runPrim(from);
            case KRUSKAL -> runKruskal();
            default -> showResult("RESULTADO", Color.web("#d8d2c0"), "Algoritmo desconocido.");
        }
    }

    // =====================================================================
    // Un método por algoritmo
    // =====================================================================

    /** Puntos 69 a 71: ruta más barata entre origen y destino. */
    private void runDijkstra(Node from, Node to) {
        PathResult r = Dijkstra.shortestPath(city, from.getId(), to.getId());
        if (!r.isFound()) {
            map.clearHighlight();
            showResult("DIJKSTRA", DIJKSTRA_COLOR, "No hay ruta entre " + from.getId() + " y " + to.getId() + ".");
            return;
        }
        map.showRoute(r.path(), DIJKSTRA_COLOR);
        showResult("DIJKSTRA", DIJKSTRA_COLOR,
                "Ruta: " + ids(r) + "\n"
                        + "Tramos: " + r.steps() + "\n"
                        + String.format("Costo total: %.2f", r.totalCost()));
    }

    /** Puntos 72 y 73: calcula todos los pares y muestra la ruta elegida y los costos desde el origen. */
    private void runFloydWarshall(Node from, Node to) {
        AllPairsResult all = FloydWarshall.compute(city);
        PathResult r = all.path(from.getId(), to.getId());
        if (!r.isFound()) {
            map.clearHighlight();
            showResult("FLOYD-WARSHALL", FLOYD_COLOR, "No hay ruta entre " + from.getId() + " y " + to.getId() + ".");
            return;
        }
        map.showRoute(r.path(), FLOYD_COLOR);

        // Costo mínimo desde el origen hacia todos los demás lugares, de menor a mayor
        List<Node> others = new ArrayList<>(city.getNodes());
        others.remove(from);
        others.sort(Comparator.comparingDouble(n -> all.cost(from.getId(), n.getId())));

        StringBuilder sb = new StringBuilder();
        sb.append("Ruta: ").append(ids(r)).append("\n");
        sb.append(String.format("Costo total: %.2f%n%n", r.totalCost()));
        sb.append("Costos mínimos desde ").append(from.getId()).append(":\n");
        for (Node n : others) {
            sb.append(String.format("  %-5s %7.2f%n", n.getId(), all.cost(from.getId(), n.getId())));
        }
        showResult("FLOYD-WARSHALL", FLOYD_COLOR, sb.toString());
    }

    /** Puntos 74 y 75: árbol de expansión mínima con Prim, creciendo desde el origen. */
    private void runPrim(Node from) {
        SpanningTree tree = Prim.compute(city, from.getId());
        map.showEdges(tree.edges(), PRIM_COLOR);
        showResult("PRIM (desde " + from.getId() + ")", PRIM_COLOR, treeText(tree));
    }

    /** Puntos 76 y 77: árbol de expansión mínima con Kruskal, y comparación con Prim. */
    private void runKruskal() {
        SpanningTree tree = Kruskal.compute(city);
        map.showEdges(tree.edges(), KRUSKAL_COLOR);

        SpanningTree prim = Prim.compute(city, "C-01");
        boolean sameEdges = new HashSet<>(tree.edges()).equals(new HashSet<>(prim.edges()));
        String comparison = String.format("%nComparación con Prim:%n  Costo Prim: %.2f%n  ¿Mismas conexiones? %s",
                prim.totalCost(), sameEdges ? "sí" : "no");

        showResult("KRUSKAL", KRUSKAL_COLOR, treeText(tree) + comparison);
    }

    /** Texto de un árbol: número de conexiones, costo total y la lista en el orden elegido. */
    private String treeText(SpanningTree tree) {
        StringBuilder sb = new StringBuilder();
        sb.append("Conexiones: ").append(tree.edges().size()).append("\n");
        sb.append(String.format("Costo total: %.2f%n", tree.totalCost()));
        sb.append("¿Conecta toda la ciudad? ").append(tree.spans(city) ? "sí" : "no").append("\n\n");
        int i = 1;
        for (Edge e : tree.edges()) {
            sb.append(String.format("%2d. %-13s %6.2f%n", i++, e, e.getCost()));
        }
        return sb.toString();
    }

    /** Convierte una ruta en texto: "C-01 → R-04 → R-05". */
    private static String ids(PathResult r) {
        return r.path().stream().map(Node::getId).collect(Collectors.joining(" → "));
    }
}
