package pathfindead.algorithms;

import pathfindead.model.Edge;
import pathfindead.model.Graph;

import java.util.List;

/** Árbol de expansión: conexiones elegidas y su costo total. */
public record SpanningTree(List<Edge> edges) {

    public SpanningTree {
        edges = List.copyOf(edges);
    }

    /** Suma de los costos actuales de las conexiones del árbol. */
    public double totalCost() {
        double total = 0;
        for (Edge e : edges) {
            total += e.getCost();
        }
        return total;
    }

    /** true si el árbol conecta todos los lugares del grafo (tiene n - 1 conexiones). */
    public boolean spans(Graph graph) {
        return edges.size() == graph.nodeCount() - 1;
    }

    @Override
    public String toString() {
        return String.format("%d conexiones, costo total %.2f", edges.size(), totalCost());
    }
}
