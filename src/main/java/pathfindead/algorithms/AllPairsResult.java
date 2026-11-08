package pathfindead.algorithms;

import pathfindead.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Resultado de Floyd-Warshall: costos mínimos y rutas entre todos los pares de lugares.
 */
public final class AllPairsResult {

    private final List<Node> nodes;
    private final Map<String, Integer> index;
    private final double[][] dist;
    private final int[][] next;

    AllPairsResult(List<Node> nodes, Map<String, Integer> index, double[][] dist, int[][] next) {
        this.nodes = List.copyOf(nodes);
        this.index = Map.copyOf(index);
        this.dist = dist;
        this.next = next;
    }

    /** Lugares en el orden en que aparecen en la matriz. */
    public List<Node> getNodes() { return nodes; }

    public int size() { return nodes.size(); }

    /** Costo mínimo para ir de un lugar a otro (infinito si no hay ruta). */
    public double cost(String fromId, String toId) {
        return dist[indexOf(fromId)][indexOf(toId)];
    }

    /** Ruta mínima completa entre dos lugares, reconstruida con la matriz next. */
    public PathResult path(String fromId, String toId) {
        int i = indexOf(fromId);
        int j = indexOf(toId);
        if (next[i][j] == -1) {
            return PathResult.notFound();
        }
        List<Node> path = new ArrayList<>();
        path.add(nodes.get(i));
        while (i != j) {
            i = next[i][j];
            path.add(nodes.get(i));
        }
        return new PathResult(path, dist[indexOf(fromId)][j]);
    }

    private int indexOf(String id) {
        Integer i = index.get(id);
        if (i == null) {
            throw new IllegalArgumentException("Lugar inexistente: " + id);
        }
        return i;
    }
}