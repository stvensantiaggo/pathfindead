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
        System.out.println();

        for (Node node : city.getNodes()) {
            System.out.println(node + " [" + node.getType() + ", " + node.getZone() + "]");
            for (Edge edge : city.getEdges(node)) {
                Node vecino = edge.other(node);
                System.out.printf("   -> %s (distancia %.2f)%n", vecino, edge.getDistance());
            }
        }
    }
}

