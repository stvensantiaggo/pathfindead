package pathfindead.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph {
    // Búsqueda rápida de un lugar por su id (ej. "C-01")
    private final Map<String, Node> nodesById = new LinkedHashMap<>();
    // La lista de adyacencia: cada lugar con las conexiones que lo tocan
    private final Map<Node, List<Edge>> adjacency = new LinkedHashMap<>();

    public void addNode(Node node) {
        if (nodesById.containsKey(node.getId())) {
            throw new IllegalArgumentException("Ya existe un lugar con el id " + node.getId());
        }
        nodesById.put(node.getId(), node);
        adjacency.put(node, new ArrayList<>());
    }

    public boolean hasNode(String id) {
        return nodesById.containsKey(id);
    }

    public Node getNode(String id) {
        Node node = nodesById.get(id);
        if (node == null) {
            throw new IllegalArgumentException("No existe un lugar con el id " + id);
        }
        return node;
    }

    public Edge connect(String idA, String idB) {
        Node a = getNode(idA);
        Node b = getNode(idB);
        Edge edge = new Edge(a, b);
        if (adjacency.get(a).contains(edge)) {
            throw new IllegalArgumentException("Ya existe la conexión " + edge);
        }
        adjacency.get(a).add(edge);
        adjacency.get(b).add(edge);
        return edge;
    }

    /** Elimina la conexión entre dos lugares. Devuelve false si no existía. */
    public boolean disconnect(String idA, String idB) {
        Node a = getNode(idA);
        Node b = getNode(idB);
        Edge edge = new Edge(a, b); // solo sirve para comparar: Edge.equals ignora el orden
        boolean removed = adjacency.get(a).remove(edge);
        adjacency.get(b).remove(edge);
        return removed;
    }

    public List<Edge> getEdges(Node node) {
        List<Edge> edges = adjacency.get(node);
        if (edges == null) {
            throw new IllegalArgumentException("El lugar " + node + " no está en el grafo");
        }
        return Collections.unmodifiableList(edges);
    }

    public List<Edge> getEdges(String id) {
        return getEdges(getNode(id));
    }

    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodesById.values());
    }

    /** Todas las conexiones, cada una una sola vez. */
    public Set<Edge> getAllEdges() {
        Set<Edge> all = new LinkedHashSet<>();
        for (List<Edge> edges : adjacency.values()) {
            all.addAll(edges);
        }
        return all;
    }

    public int nodeCount() {
        return nodesById.size();
    }

    public int edgeCount() {
        return getAllEdges().size();
    }
}


