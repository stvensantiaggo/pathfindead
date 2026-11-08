package pathfindead;

import pathfindead.algorithms.Dijkstra;
import pathfindead.algorithms.PathResult;
import pathfindead.model.CityFactory;
import pathfindead.model.Edge;
import pathfindead.model.Graph;

public class DijkstraDemo {
    public static void main(String[] args) {
        Graph city = CityFactory.createCity();

        System.out.println("=== Rutas de menor costo (Dijkstra) ===");
        String[][] viajes = {
                {"C-01", "Z-01"},
                {"C-01", "Z-02"},
                {"R-06", "H-02"},
                {"T-01", "Z-02"}
        };
        for (String[] v : viajes) {
            System.out.printf("%s -> %s : %s%n", v[0], v[1], Dijkstra.shortestPath(city, v[0], v[1]));
        }

        // Una horda invade C-01 <-> R-04: el dron debe desviarse
        System.out.println("\n=== Una horda invade C-01 <-> R-04 ===");
        for (Edge e : city.getEdges("C-01")) {
            if (e.other(city.getNode("C-01")).getId().equals("R-04")) {
                e.updateHazards(5, 3, 0);
            }
        }
        PathResult desvio = Dijkstra.shortestPath(city, "C-01", "Z-02");
        System.out.println("C-01 -> Z-02 : " + desvio);
    }
}