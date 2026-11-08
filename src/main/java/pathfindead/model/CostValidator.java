package pathfindead.model;

import java.util.ArrayList;
import java.util.List;

/** Verifica que todos los costos del grafo sean válidos (positivos o cero). */
public final class CostValidator {

    private CostValidator() { }

    /** Un costo es válido si es un número y es mayor o igual a cero. */
    public static boolean isValid(double cost) {
        return !Double.isNaN(cost) && cost >= 0;
    }

    /** Devuelve la lista de conexiones con algún costo inválido (vacía si todo está bien). */
    public static List<Edge> findInvalid(Graph city) {
        List<Edge> invalid = new ArrayList<>();
        for (Edge e : city.getAllEdges()) {
            if (!isValid(e.getOriginalCost()) || !isValid(e.getCurrentCost())) {
                invalid.add(e);
            }
        }
        return invalid;
    }

    /** true si todas las conexiones tienen costos válidos. */
    public static boolean allCostsValid(Graph city) {
        return findInvalid(city).isEmpty();
    }

    /** Lanza un error si alguna conexión tiene un costo inválido. */
    public static void validate(Graph city) {
        List<Edge> invalid = findInvalid(city);
        if (!invalid.isEmpty()) {
            throw new IllegalStateException("Conexiones con costo inválido: " + invalid);
        }
    }
}