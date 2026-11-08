package pathfindead.algorithms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pathfindead.model.CityFactory;
import pathfindead.model.Edge;
import pathfindead.model.Graph;
import pathfindead.model.Node;
import pathfindead.model.NodeType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FloydWarshallTest {

    private Graph small;

    @BeforeEach
    void crearGrafoPequeno() {
        small = new Graph();
        small.addNode(new Node("A", "Lugar A", NodeType.SHELTER, "Test", 0, 0));
        small.addNode(new Node("B", "Lugar B", NodeType.SHELTER, "Test", 3, 0));
        small.addNode(new Node("C", "Lugar C", NodeType.SHELTER, "Test", 3, 4));
        small.addNode(new Node("D", "Lugar D", NodeType.SHELTER, "Test", 20, 20));
        small.connect("A", "B");   // 3
        small.connect("B", "C");   // 4
        small.connect("A", "C");   // 5
    }

    private List<String> ids(PathResult r) {
        return r.path().stream().map(Node::getId).toList();
    }

    // ---------- Grafo pequeño ----------

    @Test
    void calculaLosCostosEntreTodosLosPares() {
        AllPairsResult r = FloydWarshall.compute(small);
        assertEquals(3.0, r.cost("A", "B"), 1e-9);
        assertEquals(4.0, r.cost("B", "C"), 1e-9);
        assertEquals(5.0, r.cost("A", "C"), 1e-9);
    }

    @Test
    void laDiagonalEsCeroYLaMatrizEsSimetrica() {
        AllPairsResult r = FloydWarshall.compute(small);
        for (String x : List.of("A", "B", "C")) {
            assertEquals(0.0, r.cost(x, x), 1e-9);
            for (String y : List.of("A", "B", "C")) {
                assertEquals(r.cost(x, y), r.cost(y, x), 1e-9);
            }
        }
    }

    @Test
    void conZombisEncuentraElCaminoIndirecto() {
        for (Edge e : small.getEdges("A")) {
            if (e.other(small.getNode("A")).getId().equals("C")) {
                e.setZombieDanger(5);   // la directa pasa a costar 10
            }
        }
        AllPairsResult r = FloydWarshall.compute(small);
        assertEquals(7.0, r.cost("A", "C"), 1e-9);
        assertEquals(List.of("A", "B", "C"), ids(r.path("A", "C")));
    }

    @Test
    void lugarAisladoQuedaEnInfinito() {
        AllPairsResult r = FloydWarshall.compute(small);
        assertTrue(Double.isInfinite(r.cost("A", "D")));
        assertFalse(r.path("A", "D").isFound());
    }

    @Test
    void rechazaLugaresInexistentes() {
        AllPairsResult r = FloydWarshall.compute(small);
        assertThrows(IllegalArgumentException.class, () -> r.cost("A", "X"));
    }

    // ---------- Ciudad real ----------

    @Test
    void enLaCiudadCoincideConElEjemploDeDijkstra() {
        AllPairsResult r = FloydWarshall.compute(CityFactory.createCity());
        assertEquals(14, r.size());
        assertEquals(21.28, r.cost("C-01", "Z-01"), 0.01);
        assertEquals(List.of("C-01", "H-01", "Z-01"), ids(r.path("C-01", "Z-01")));
    }

    @Test
    void coincideConDijkstraEnLos196Pares() {
        Graph city = CityFactory.createCity();
        AllPairsResult fw = FloydWarshall.compute(city);
        for (Node from : city.getNodes()) {
            for (Node to : city.getNodes()) {
                PathResult d = Dijkstra.shortestPath(city, from.getId(), to.getId());
                assertEquals(d.totalCost(), fw.cost(from.getId(), to.getId()), 1e-9,
                        "Diferencia entre " + from.getId() + " y " + to.getId());
            }
        }
    }
}