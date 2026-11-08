package pathfindead.model;

import java.util.Objects;

public class Edge {
    public static final int MIN_DANGER = 0;
    public static final int MAX_DANGER = 5;

    private final Node a;
    private final Node b;
    private final double distance;

    private int zombieDanger = 0;
    private int hostileDanger = 0;

    public Edge(Node a, Node b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        if (a.equals(b)) {
            throw new IllegalArgumentException("Una conexión necesita dos lugares distintos");
        }
        this.a = a;
        this.b = b;
        this.distance = Math.hypot(a.getX() - b.getX(), a.getY() - b.getY());
    }

    public Node getA() { return a; }
    public Node getB() { return b; }
    public double getDistance() { return distance; }

    public int getZombieDanger() { return zombieDanger; }

    public void setZombieDanger(int level) {
        checkLevel(level, "zombi");
        this.zombieDanger = level;
    }

    public int getHostileDanger() { return hostileDanger; }

    public void setHostileDanger(int level) {
        checkLevel(level, "hostil");
        this.hostileDanger = level;
    }

    /** Valida que un nivel de peligro esté entre 0 y 5. */
    private static void checkLevel(int level, String tipo) {
        if (level < MIN_DANGER || level > MAX_DANGER) {
            throw new IllegalArgumentException(
                    "Peligro " + tipo + " inválido (" + level + "). Debe estar entre "
                            + MIN_DANGER + " y " + MAX_DANGER);
        }
    }

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