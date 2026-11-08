package pathfindead.algorithms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pathfindead.model.CityFactory;
import pathfindead.model.Edge;
import pathfindead.model.Graph;
import pathfindead.model.Node;
import pathfindead.model.NodeType;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PrimTest {

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

    // ---------- Grafo pequeño ----------

    @Test
    void elArbolTieneNMenosUnaConexionesYCostoMinimo() {
        SpanningTree t = Prim.compute(small, "A");
        assertEquals(3, t.edges().size());
        assertEquals(10.0, t.totalCost(), 1e-9);
        assertTrue(t.spans(small));
    }

    @Test
    void noUsaLaDiagonalMasCara() {
        SpanningTree t = Prim.compute(small, "A");
        Node a = small.getNode("A");
        Node c = small.getNode("C");
        for (Edge e : t.edges()) {
            assertFalse(e.connects(a) && e.connects(c), "No debería usar A-C");
        }
    }

    @Test
    void elCostoNoDependeDelLugarDeInicio() {
        for (String start : new String[]{"A", "B", "C", "D"}) {
            assertEquals(10.0, Prim.compute(small, start).totalCost(), 1e-9);
        }
    }

    @Test
    void conUnLugarAisladoNoConectaTodo() {
        small.addNode(new Node("E", "Lugar E", NodeType.SHELTER, "Test", 20, 20));
        SpanningTree t = Prim.compute(small, "A");
        assertFalse(t.spans(small));
    }

    @Test
    void rechazaLugarInexistente() {
        assertThrows(IllegalArgumentException.class, () -> Prim.compute(small, "X"));
    }

    // ---------- Ciudad real ----------

    @Test
    void enLaCiudadConectaLos14LugaresCon13Conexiones() {
        Graph city = CityFactory.createCity();
        SpanningTree t = Prim.compute(city, "C-01");
        assertEquals(13, t.edges().size());
        assertTrue(t.spans(city));

        // Todos los lugares aparecen en alguna conexión del árbol
        Set<String> cubiertos = new HashSet<>();
        for (Edge e : t.edges()) {
            cubiertos.add(e.getA().getId());
            cubiertos.add(e.getB().getId());
        }
        assertEquals(14, cubiertos.size());
    }

    @Test
    void enLaCiudadElCostoMinimoEs72_86DesdeCualquierLugar() {
        Graph city = CityFactory.createCity();
        for (Node n : city.getNodes()) {
            assertEquals(72.86, Prim.compute(city, n.getId()).totalCost(), 0.01,
                    "Distinto costo empezando en " + n.getId());
        }
    }
}