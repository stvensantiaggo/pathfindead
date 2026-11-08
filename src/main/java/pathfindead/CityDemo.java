package pathfindead;

import pathfindead.model.CityFactory;
import pathfindead.model.Edge;
import pathfindead.model.Graph;
import pathfindead.model.Node;

public class CityDemo {
    public static void main(String[] args) {
        Graph city = CityFactory.createCity();
        System.out.println("Lugares: " + city.nodeCount() + " | Conexiones: " + city.edgeCount());
        System.out.println("Todo conectado: " + city.isConnected());
        System.out.println("Costos validos (>= 0): " + pathfindead.model.CostValidator.allCostsValid(city));
        System.out.println();

        for (Node node : city.getNodes()) {
            System.out.println(node + " [" + node.getType() + ", " + node.getZone() + "]");
            for (Edge edge : city.getEdges(node)) {
                Node vecino = edge.other(node);
                System.out.printf("   -> %s (distancia %.2f)%n", vecino, edge.getDistance());
            }
        }

        System.out.println("\n=== Distancias, peligros y costos de las 24 conexiones ===");
        for (Edge e : city.getAllEdges()) {
            System.out.printf("%-15s dist %6.2f | Z %d | H %d | V %d | original %6.2f | actual %6.2f%n",
                    e, e.getDistance(), e.getZombieDanger(), e.getHostileDanger(),
                    e.getWindRisk(), e.getOriginalCost(), e.getCurrentCost());
        }

        // ---------- Simulación: una horda invade C-01 <-> R-04 ----------
        Edge corredor = null;
        for (Edge e : city.getEdges("C-01")) {
            if (e.other(city.getNode("C-01")).getId().equals("R-04")) {
                corredor = e;
            }
        }

        System.out.println("\n=== Simulacion de cambio de peligro ===");
        System.out.printf("Antes:      %s costo actual %.2f%n", corredor, corredor.getCurrentCost());

        corredor.updateHazards(5, 1, 0);
        System.out.printf("Con horda:  %s costo actual %.2f (original %.2f)%n",
                corredor, corredor.getCurrentCost(), corredor.getOriginalCost());

        corredor.resetToOriginal();
        System.out.printf("Restaurado: %s costo actual %.2f%n", corredor, corredor.getCurrentCost());
    }
}