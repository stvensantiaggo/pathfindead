package pathfindead.simulation;

import pathfindead.model.Graph;
import pathfindead.model.Node;
import pathfindead.model.NodeType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Estado de todos los refugios de la ciudad. */
public class SupplyState {

    private final Map<String, Shelter> shelters = new LinkedHashMap<>();

    /** Crea un Shelter por cada ubicación de tipo SHELTER, todos con la misma necesidad. */
    public SupplyState(Graph city, int needPerShelter) {
        for (Node n : city.getNodes()) {
            if (n.getType() == NodeType.SHELTER) {
                shelters.put(n.getId(), new Shelter(n, needPerShelter));
            }
        }
    }

    public boolean isShelter(Node node) {
        return shelters.containsKey(node.getId());
    }

    /** Estado del refugio que está en ese lugar (o null si el lugar no es un refugio). */
    public Shelter get(Node node) {
        return shelters.get(node.getId());
    }

    public Collection<Shelter> all() {
        return Collections.unmodifiableCollection(shelters.values());
    }

    /** Cuántos refugios ya recibieron todo lo que necesitaban. */
    public int suppliedCount() {
        int count = 0;
        for (Shelter s : shelters.values()) {
            if (s.isSupplied()) count++;
        }
        return count;
    }

    public boolean allSupplied() {
        return suppliedCount() == shelters.size();
    }
}