package pathfindead.model;

import java.util.Objects;

public class Edge {
    public static final int MIN_DANGER = 0;
    public static final int MAX_DANGER = 5;

    private final Node a;
    private final Node b;
    private final double distance;

    // Peligros actuales
    private int zombieDanger = 0;
    private int hostileDanger = 0;
    private int windRisk = 0;

    // Punto 37: costo original y los peligros con los que empezó
    private double originalCost;
    private int originalZombieDanger = 0;
    private int originalHostileDanger = 0;
    private int originalWindRisk = 0;

    // Punto 38: costo actual
    private double currentCost;

    public Edge(Node a, Node b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        if (a.equals(b)) {
            throw new IllegalArgumentException("Una conexión necesita dos lugares distintos");
        }
        this.a = a;
        this.b = b;
        this.distance = Math.hypot(a.getX() - b.getX(), a.getY() - b.getY());
        // Sin peligros, el costo es igual a la distancia
        this.currentCost = distance;
        this.originalCost = distance;
    }

    public Node getA() { return a; }
    public Node getB() { return b; }
    public double getDistance() { return distance; }

    // ---------- Costos ----------

    public double getOriginalCost() { return originalCost; }
    public double getCurrentCost() { return currentCost; }

    /** Costo que usan los algoritmos: siempre el actual. */
    public double getCost() { return currentCost; }

    // ---------- Peligros ----------

    public int getZombieDanger() { return zombieDanger; }

    public void setZombieDanger(int level) {
        checkLevel(level, "zombi");
        this.zombieDanger = level;
        recalculateCost();
    }

    public int getHostileDanger() { return hostileDanger; }

    public void setHostileDanger(int level) {
        checkLevel(level, "hostil");
        this.hostileDanger = level;
        recalculateCost();
    }

    public int getWindRisk() { return windRisk; }

    public void setWindRisk(int level) {
        checkLevel(level, "de viento");
        this.windRisk = level;
        recalculateCost();
    }

    // ---------- Punto 39: actualizar el costo ----------

    /**
     * Cambia los tres peligros a la vez y recalcula el costo actual.
     * Si algún valor es inválido, no cambia nada.
     */
    public void updateHazards(int zombies, int hostiles, int wind) {
        checkLevel(zombies, "zombi");
        checkLevel(hostiles, "hostil");
        checkLevel(wind, "de viento");
        this.zombieDanger = zombies;
        this.hostileDanger = hostiles;
        this.windRisk = wind;
        recalculateCost();
    }

    /** Guarda los peligros y el costo actuales como los originales. */
    public void saveAsOriginal() {
        this.originalZombieDanger = zombieDanger;
        this.originalHostileDanger = hostileDanger;
        this.originalWindRisk = windRisk;
        this.originalCost = currentCost;
    }

    /** Devuelve el corredor a sus peligros y costo originales. */
    public void resetToOriginal() {
        updateHazards(originalZombieDanger, originalHostileDanger, originalWindRisk);
    }

    /** Recalcula el costo actual con la fórmula. */
    private void recalculateCost() {
        this.currentCost = CostFormula.calculate(distance, zombieDanger, hostileDanger, windRisk);
    }

    /** Valida que un nivel de peligro esté entre 0 y 5. */
    private static void checkLevel(int level, String tipo) {
        if (level < MIN_DANGER || level > MAX_DANGER) {
            throw new IllegalArgumentException(
                    "Peligro " + tipo + " inválido (" + level + "). Debe estar entre "
                            + MIN_DANGER + " y " + MAX_DANGER);
        }
    }

    // ---------- Fase 3 ----------

    /** Devuelve el extremo contrario al nodo indicado. */
    public Node other(Node node) {
        if (node.equals(a)) return b;
        if (node.equals(b)) return a;
        throw new IllegalArgumentException(node + " no pertenece a esta conexión");
    }

    public boolean connects(Node node) {
        return node.equals(a) || node.equals(b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge that)) return false;
        return (a.equals(that.a) && b.equals(that.b))
                || (a.equals(that.b) && b.equals(that.a));
    }

    @Override
    public int hashCode() {
        return a.hashCode() + b.hashCode();
    }

    @Override
    public String toString() {
        return a.getId() + " <-> " + b.getId();
    }
}