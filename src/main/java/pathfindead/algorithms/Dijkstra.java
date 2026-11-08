package pathfindead.algorithms;

import pathfindead.model.Edge;
import pathfindead.model.Graph;
import pathfindead.model.Node;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/** Algoritmo de Dijkstra: ruta de menor costo entre dos lugares. */
public final class Dijkstra {

    private Dijkstra() { }

    /** Par (lugar, costo acumulado) que se guarda en la cola de prioridad. */
    private record NodeCost(Node node, double cost) { }

    public static PathResult shortestPath(Graph graph, String startId, String endId) {
        if (!graph.hasNode(startId) || !graph.hasNode(endId)) {
            throw new IllegalArgumentException("Origen o destino inexistente: " + startId + ", " + endId);
        }
        Node start = graph.getNode(startId);
        Node end = graph.getNode(endId);

        // 1. Costo acumulado de cada lugar: infinito, excepto el origen
        Map<Node, Double> cost = new HashMap<>();
        for (Node n : graph.getNodes()) {
            cost.put(n, Double.POSITIVE_INFINITY);
        }
        cost.put(start, 0.0);

        // Desde qué lugar se llegó a cada lugar (para reconstruir la ruta)
        Map<Node, Node> previous = new HashMap<>();
        Set<Node> visited = new HashSet<>();

        // 2. Cola de prioridad: siempre sale primero el de menor costo
        PriorityQueue<NodeCost> queue = new PriorityQueue<>(Comparator.comparingDouble(NodeCost::cost));
        queue.add(new NodeCost(start, 0.0));

        while (!queue.isEmpty()) {
            Node current = queue.poll().node();

            if (!visited.add(current)) continue;   // ya se procesó
            if (current.equals(end)) break;         // llegamos al destino

            // 3. Relajar las conexiones del lugar actual
            for (Edge edge : graph.getEdges(current)) {
                Node neighbor = edge.other(current);
                if (visited.contains(neighbor)) continue;

                double newCost = cost.get(current) + edge.getCost();
                if (newCost < cost.get(neighbor)) {
                    cost.put(neighbor, newCost);
                    previous.put(neighbor, current);
                    queue.add(new NodeCost(neighbor, newCost));
                }
            }
        }

        // 4. Si el destino sigue en infinito, no hay ruta
        if (cost.get(end).isInfinite()) {
            return PathResult.notFound();
        }

        // 5. Reconstruir la ruta desde el destino hacia el origen
        LinkedList<Node> path = new LinkedList<>();
        for (Node at = end; at != null; at = previous.get(at)) {
            path.addFirst(at);
        }
        return new PathResult(path, cost.get(end));
    }
}