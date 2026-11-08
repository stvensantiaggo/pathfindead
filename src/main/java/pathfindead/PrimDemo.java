package pathfindead;

import pathfindead.algorithms.Prim;
import pathfindead.algorithms.SpanningTree;
import pathfindead.model.CityFactory;
import pathfindead.model.Edge;
import pathfindead.model.Graph;

public class PrimDemo {
    public static void main(String[] args) {
        Graph city = CityFactory.createCity();
        SpanningTree tree = Prim.compute(city, "C-01");

        System.out.println("=== Arbol de expansion minima (Prim desde C-01) ===");
        int i = 1;
        for (Edge e : tree.edges()) {
            System.out.printf("%2d. %-15s costo %6.2f%n", i++, e, e.getCost());
        }
        System.out.println("Resultado: " + tree);
        System.out.println("Conecta toda la ciudad? " + tree.spans(city));

        double todas = 0;
        for (Edge e : city.getAllEdges()) {
            todas += e.getCost();
        }
        System.out.printf("%nCosto de las 24 conexiones: %.2f%n", todas);
        System.out.printf("Costo del arbol (13):      %.2f%n", tree.totalCost());
        System.out.printf("Ahorro:                    %.1f %%%n", 100 * (1 - tree.totalCost() / todas));
    }
}