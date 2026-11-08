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

class KruskalTest {

    private Graph small;

    @BeforeEach
    void crearGrafoPequeno() {
        small = new Graph();
        small.addNode(new Node("A", "Lugar A", NodeType.SHELTER, "Test", 0, 0));
        small.addNode(new Node("B", "Lugar B", NodeType.SHELTER, "Test", 3, 0));
        small.addNode(new Node("C", "Lugar C", NodeType.SHELTER, "Test", 3, 4));
        small.addNode(new Node("D", "Lugar D", NodeType.SHELTER, "Test", 0, 4));
        small.connect("A", "B");   // 3
        small.connect("B", "C");   // 4
        small.connect("C", "D");   // 3
        small.connect("D", "A");   // 4
        small.connect("A", "C");   // 5 (diagonal)
    }

    @Test
    void elArbolTieneNMenosUnaConexionesYCostoMinimo() {
        SpanningTree t = Kruskal.compute(small);
        assertEquals(3, t.edges().size());
        assertEquals(10.0, t.totalCost(), 1e-9);
        assertTrue(t.spans(small));
    }

    @Test
    void noUsaLaDiagonalMasCara() {
        SpanningTree t = Kruskal.compute(small);
        Node a = small.getNode("A");
        Node c = small.getNode("C");
        for (Edge e : t.edges()) {
            assertFalse(e.connects(a) && e.connects(c), "No debería usar A-C");
        }
    }

    @Test
    void eligeLasConexionesEnOrdenDeCosto() {
        List<Edge> edges = Kruskal.compute(CityFactory.createCity()).edges();
        for (int i = 1; i < edges.size(); i++) {
            assertTrue(edges.get(i - 1).getCost() <= edges.get(i).getCost());
        }
    }

    @Test
    void conUnLugarAisladoNoConectaTodo() {
        small.addNode(new Node("E", "Lugar E", NodeType.SHELTER, "Test", 20, 20));
        assertFalse(Kruskal.compute(small).spans(small));
    }

    @Test
    void enLaCiudadConecta14LugaresConCosto72_86() {
        Graph city = CityFactory.createCity();
        SpanningTree t = Kruskal.compute(city);
        assertEquals(13, t.edges().size());
        assertTrue(t.spans(city));
        assertEquals(72.86, t.totalCost(), 0.01);
    }
}