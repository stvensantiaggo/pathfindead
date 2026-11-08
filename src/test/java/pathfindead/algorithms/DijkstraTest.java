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

class DijkstraTest {

    private Graph small;

    /** Se ejecuta antes de cada prueba: crea el triángulo A-B-C y el nodo aislado D. */
    @BeforeEach
    void crearGrafoPequeno() {
        small = new Graph();
        small.addNode(new Node("A", "Lugar A", NodeType.SHELTER, "Test", 0, 0));
        small.addNode(new Node("B", "Lugar B", NodeType.SHELTER, "Test", 3, 0));
        small.addNode(new Node("C", "Lugar C", NodeType.SHELTER, "Test", 3, 4));
        small.addNode(new Node("D", "Lugar D", NodeType.SHELTER, "Test", 20, 20));
        small.connect("A", "B");   // distancia 3
        small.connect("B", "C");   // distancia 4
        small.connect("A", "C");   // distancia 5
    }

    private List<String> ids(PathResult r) {
        return r.path().stream().map(Node::getId).toList();
    }

    // ---------- Punto 43: grafo pequeño ----------

    @Test
    void sinPeligrosTomaLaRutaDirecta() {
        PathResult r = Dijkstra.shortestPath(small, "A", "C");
        assertEquals(List.of("A", "C"), ids(r));
        assertEquals(5.0, r.totalCost(), 1e-9);
    }

    @Test
    void conZombisEnLaDirectaSeDesvia() {
        for (Edge e : small.getEdges("A")) {
            if (e.other(small.getNode("A")).getId().equals("C")) {
                e.setZombieDanger(5);   // costo 5 × 2 = 10
            }
        }
        PathResult r = Dijkstra.shortestPath(small, "A", "C");
        assertEquals(List.of("A", "B", "C"), ids(r));
        assertEquals(7.0, r.totalCost(), 1e-9);
    }

    @Test
    void origenIgualADestinoTieneCostoCero() {
        PathResult r = Dijkstra.shortestPath(small, "B", "B");
        assertEquals(List.of("B"), ids(r));
        assertEquals(0.0, r.totalCost(), 1e-9);
    }

    @Test
    void lugarAisladoNoTieneRuta() {
        PathResult r = Dijkstra.shortestPath(small, "A", "D");
        assertFalse(r.isFound());
        assertTrue(Double.isInfinite(r.totalCost()));
    }

    @Test
    void rechazaLugaresInexistentes() {
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.shortestPath(small, "A", "X"));
    }

    // ---------- Punto 44: menor costo en la ciudad real ----------

    @Test
    void enLaCiudadEvitaElCorredorDirectoPeligroso() {
        Graph city = CityFactory.createCity();
        PathResult r = Dijkstra.shortestPath(city, "C-01", "Z-01");
        assertEquals(List.of("C-01", "H-01", "Z-01"), ids(r));
        assertEquals(21.28, r.totalCost(), 0.01);
    }

    @Test
    void rutaDelCentroAViaTasco() {
        Graph city = CityFactory.createCity();
        PathResult r = Dijkstra.shortestPath(city, "C-01", "Z-02");
        assertEquals(List.of("C-01", "R-04", "R-05", "Z-02"), ids(r));
        assertEquals(16.57, r.totalCost(), 0.01);
    }
}