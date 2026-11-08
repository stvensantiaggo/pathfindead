package pathfindead.model;

/**
 * Fórmula que combina la distancia y los peligros de un corredor en un solo costo.
 * costo = distancia × (1 + 0.20·zombis + 0.15·hostiles + 0.05·viento)
 */
public final class CostFormula {

    public static final double ZOMBIE_WEIGHT = 0.20;
    public static final double HOSTILE_WEIGHT = 0.15;
    public static final double WIND_WEIGHT = 0.05;

    // Clase de utilidad: no se crean objetos de ella
    private CostFormula() { }

    public static double calculate(double distance, int zombies, int hostiles, int wind) {
        if (distance < 0) {
            throw new IllegalArgumentException("La distancia no puede ser negativa: " + distance);
        }
        double factor = 1
                + ZOMBIE_WEIGHT * zombies
                + HOSTILE_WEIGHT * hostiles
                + WIND_WEIGHT * wind;
        double cost = distance * factor;
        if (!CostValidator.isValid(cost)) {
            throw new IllegalStateException("Costo inválido calculado: " + cost);
        }
        return cost;
    }
}