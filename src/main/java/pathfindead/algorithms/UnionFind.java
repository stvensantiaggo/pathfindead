package pathfindead.algorithms;

import java.util.HashMap;
import java.util.Map;

/**
 * Estructura Union-Find (conjuntos disjuntos).
 * Agrupa lugares y responde rápido si dos lugares pertenecen al mismo grupo.
 */
public final class UnionFind {

    // Cada lugar apunta a su "padre"; el líder del grupo se apunta a sí mismo
    private final Map<String, String> parent = new HashMap<>();
    // Altura aproximada de cada grupo (para unir el pequeño debajo del grande)
    private final Map<String, Integer> rank = new HashMap<>();

    /** Agrega un lugar como un grupo de un solo elemento. */
    public void add(String id) {
        parent.putIfAbsent(id, id);
        rank.putIfAbsent(id, 0);
    }

    /** Devuelve el líder del grupo del lugar (con compresión de caminos). */
    public String find(String id) {
        String p = parent.get(id);
        if (p == null) {
            throw new IllegalArgumentException("Lugar no registrado: " + id);
        }
        if (!p.equals(id)) {
            p = find(p);
            parent.put(id, p);   // compresión: apunta directo al líder
        }
        return p;
    }

    /**
     * Une los grupos de dos lugares.
     * @return true si estaban separados; false si ya estaban en el mismo grupo.
     */
    public boolean union(String a, String b) {
        String rootA = find(a);
        String rootB = find(b);
        if (rootA.equals(rootB)) {
            return false;
        }
        int rankA = rank.get(rootA);
        int rankB = rank.get(rootB);
        if (rankA < rankB) {
            parent.put(rootA, rootB);
        } else if (rankA > rankB) {
            parent.put(rootB, rootA);
        } else {
            parent.put(rootB, rootA);
            rank.put(rootA, rankA + 1);
        }
        return true;
    }

    /** true si los dos lugares están en el mismo grupo. */
    public boolean connected(String a, String b) {
        return find(a).equals(find(b));
    }
}