package pathfindead.algorithms;

import pathfindead.model.Edge;
import pathfindead.model.Graph;
import pathfindead.model.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/** Algoritmo de Prim: árbol de expansión mínima creciendo desde un lugar. */
public final class Prim {

    private Prim() { }

    /** Construye el árbol empezando desde el lugar indicado. */
    public static SpanningTree compute(Graph graph, String startId) {
        if (!graph.hasNode(startId)) {
            throw new IllegalArgumentException("Lugar inexistente: " + startId);
        }
        Node start = graph.getNode(startId);

        Set<Node> inTree = new HashSet<>();
        List<Edge> treeEdges = new ArrayList<>();

        // Cola de conexiones candidatas: sale primero la más barata
        PriorityQueue<Edge> queue = new PriorityQueue<>(Comparator.comparingDouble(Edge::getCost));

        // 1. El árbol empieza con el lugar de inicio
        inTree.add(start);
        queue.addAll(graph.getEdges(start));

        while (!queue.isEmpty() && inTree.size() < graph.nodeCount()) {
            Edge edge = queue.poll();
            boolean aIn = inTree.contains(edge.getA());
            boolean bIn = inTree.contains(edge.getB());

            // 2. Si ambos extremos ya están en el árbol, formaría un ciclo: se descarta
            if (aIn && bIn) continue;

            // 3. Agregar el lugar nuevo y la conexión al árbol
            Node newNode = aIn ? edge.getB() : edge.getA();
            inTree.add(newNode);
            treeEdges.add(edge);

            // 4. Sus conexiones pasan a ser candidatas
            for (Edge next : graph.getEdges(newNode)) {
                if (!inTree.contains(next.other(newNode))) {
                    queue.add(next);
                }
            }
        }
        return new SpanningTree(treeEdges);
    }
}