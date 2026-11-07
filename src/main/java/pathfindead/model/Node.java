package pathfindead.model;

import java.util.Objects;

public class Node {
    private final String id;
    private final String name;
    private final NodeType type;
    private final String zone;
    private final double x;
    private final double y;

    public Node(String id, String name, NodeType type, String zone, double x, double y) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.zone = zone;
        this.x = x;
        this.y = y;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public NodeType getType() { return type; }
    public String getZone() { return zone; }
    public double getX() { return x; }
    public double getY() { return y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " (" + name + ")";
    }
}