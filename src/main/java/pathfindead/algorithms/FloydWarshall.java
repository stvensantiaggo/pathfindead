package pathfindead.algorithms;

import pathfindead.model.Edge;
import pathfindead.model.Graph;
import pathfindead.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Algoritmo de Floyd-Warshall: caminos mínimos entre todos los pares de lugares. */
public final class FloydWarshall {

    private FloydWarshall() { }

    public static AllPairsResult compute(Graph graph) {
        List<Node> nodes = new ArrayList<>(graph.getNodes());
        int n = nodes.size();

        // Cada lugar recibe un número de fila/columna en la matriz
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < n; i++) {
            index.put(nodes.get(i).getId(), i);
        }

        double[][] dist = new double[n][n];
        int[][] next = new int[n][n];

        // 1. Inicializar: 0 en la diagonal, infinito en el resto
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                    next[i][j] = j;
                } else {
                    dist[i][j] = Double.POSITIVE_INFINITY;
                    next[i][j] = -1;   // -1 significa "no hay ruta conocida"
                }
            }
        }

        // 2. Cargar las conexiones directas (el grafo es no dirigido)
        for (Edge e : graph.getAllEdges()) {
            int a = index.get(e.getA().getId());
            int b = index.get(e.getB().getId());
            double c = e.getCost();
            if (c < dist[a][b]) {
                dist[a][b] = c;
                dist[b][a] = c;
                next[a][b] = b;
                next[b][a] = a;
            }
        }

        // 3. Probar cada lugar k como punto intermedio
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    double throughK = dist[i][k] + dist[k][j];
                    if (throughK < dist[i][j]) {
                        dist[i][j] = throughK;
                        next[i][j] = next[i][k];
                    }
                }
            }
        }

        return new AllPairsResult(nodes, index, dist, next);
    }
}