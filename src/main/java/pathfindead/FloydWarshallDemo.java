package pathfindead;

import pathfindead.algorithms.AllPairsResult;
import pathfindead.algorithms.FloydWarshall;
import pathfindead.model.CityFactory;
import pathfindead.model.Graph;
import pathfindead.model.Node;

public class FloydWarshallDemo {
    public static void main(String[] args) {
        Graph city = CityFactory.createCity();
        AllPairsResult r = FloydWarshall.compute(city);

        // Encabezado de la matriz
        System.out.println("=== Matriz de costos mínimos (Floyd-Warshall) ===");
        System.out.printf("%6s", "");
        for (Node col : r.getNodes()) {
            System.out.printf("%7s", col.getId());
        }
        System.out.println();

        // Filas
        for (Node row : r.getNodes()) {
            System.out.printf("%6s", row.getId());
            for (Node col : r.getNodes()) {
                System.out.printf("%7.1f", r.cost(row.getId(), col.getId()));
            }
            System.out.println();
        }

        // Algunas rutas reconstruidas
        System.out.println("\n=== Rutas reconstruidas ===");
        System.out.println("T-01 -> Z-02 : " + r.path("T-01", "Z-02"));
        System.out.println("R-06 -> H-01 : " + r.path("R-06", "H-01"));

        // Lugar más "central": el de menor costo total hacia todos los demás
        Node mejor = null;
        double mejorSuma = Double.POSITIVE_INFINITY;
        for (Node a : r.getNodes()) {
            double suma = 0;
            for (Node b : r.getNodes()) {
                suma += r.cost(a.getId(), b.getId());
            }
            if (suma < mejorSuma) {
                mejorSuma = suma;
                mejor = a;
            }
        }
        System.out.printf("%nLugar mas central: %s (suma de costos %.2f)%n", mejor, mejorSuma);
    }
}