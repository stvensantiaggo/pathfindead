package pathfindead.algorithms;

import pathfindead.model.Node;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Resultado de buscar una ruta: la lista de lugares en orden y su costo total.
 * Si no hay ruta, la lista está vacía y el costo es infinito.
 */
public record PathResult(List<Node> path, double totalCost) {

    public PathResult {
        path = List.copyOf(path);   // lista de solo lectura
    }

    public static PathResult notFound() {
        return new PathResult(List.of(), Double.POSITIVE_INFINITY);
    }

    public boolean isFound() {
        return !path.isEmpty();
    }

    /** Número de tramos (conexiones) que tiene la ruta. */
    public int steps() {
        return Math.max(0, path.size() - 1);
    }

    @Override
    public String toString() {
        if (!isFound()) return "Sin ruta";
        String ids = path.stream().map(Node::getId).collect(Collectors.joining(" -> "));
        return String.format("%s (costo %.2f)", ids, totalCost);
    }
}