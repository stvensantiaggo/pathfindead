package pathfindead;

import pathfindead.algorithms.Kruskal;
import pathfindead.algorithms.Prim;
import pathfindead.algorithms.SpanningTree;
import pathfindead.model.CityFactory;
import pathfindead.model.Edge;
import pathfindead.model.Graph;

import java.util.HashSet;

public class MstComparisonDemo {
    public static void main(String[] args) {
        Graph city = CityFactory.createCity();
        SpanningTree prim = Prim.compute(city, "C-01");
        SpanningTree kruskal = Kruskal.compute(city);

        System.out.println("=== Orden en que cada algoritmo elige las conexiones ===");
        System.out.printf("%-4s %-22s %-22s%n", "#", "Prim (desde C-01)", "Kruskal");
        for (int i = 0; i < prim.edges().size(); i++) {
            Edge p = prim.edges().get(i);
            Edge k = kruskal.edges().get(i);
            System.out.printf("%-4d %-15s %6.2f %-15s %6.2f%n",
                    i + 1, p, p.getCost(), k, k.getCost());
        }

        System.out.println();
        System.out.println("Prim:    " + prim);
        System.out.println("Kruskal: " + kruskal);
        boolean mismas = new HashSet<>(prim.edges()).equals(new HashSet<>(kruskal.edges()));
        System.out.println("Mismas conexiones? " + mismas);
    }
}