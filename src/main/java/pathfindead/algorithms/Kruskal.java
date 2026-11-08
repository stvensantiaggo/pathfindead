package pathfindead.algorithms;

import pathfindead.model.Edge;
import pathfindead.model.Graph;
import pathfindead.model.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Algoritmo de Kruskal: árbol de expansión mínima uniendo grupos. */
public final class Kruskal {

    private Kruskal() { }

    public static SpanningTree compute(Graph graph) {
        // 1. Ordenar todas las conexiones de menor a mayor costo
        List<Edge> edges = new ArrayList<>(graph.getAllEdges());
        edges.sort(Comparator.comparingDouble(Edge::getCost));

        // 2. Cada lugar empieza en su propio grupo
        UnionFind groups = new UnionFind();
        for (Node n : graph.getNodes()) {
            groups.add(n.getId());
        }

        // 3. Tomar cada conexión si une dos grupos distintos
        List<Edge> tree = new ArrayList<>();
        int needed = graph.nodeCount() - 1;
        for (Edge e : edges) {
            if (tree.size() == needed) break;   // 4. Ya está completo
            if (groups.union(e.getA().getId(), e.getB().getId())) {
                tree.add(e);
            }
        }
        return new SpanningTree(tree);
    }
}