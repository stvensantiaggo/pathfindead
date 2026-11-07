package pathfindead.model;

import java.util.Objects;

public class Edge {
    private final Node a;
    private final Node b;
    private final double distance;

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
