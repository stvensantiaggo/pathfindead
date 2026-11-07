package pathfindead.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTest {

    private Graph graph;

    @BeforeEach
    void preparar() {
        graph = new Graph();
        graph.addNode(new Node("A", "Lugar A", NodeType.CONTROL_CENTER, "Central", 0, 0));
        graph.addNode(new Node("B", "Lugar B", NodeType.SHELTER, "Central", 3, 4));
        graph.addNode(new Node("C", "Lugar C", NodeType.SHELTER, "Norte", 6, 8));
    }

    @Test
    void buscaUnLugarPorSuId() {
        assertEquals("Lugar B", graph.getNode("B").getName());
        assertTrue(graph.hasNode("A"));
        assertFalse(graph.hasNode("Z"));
    }

    @Test
    void noPermiteIdsRepetidos() {
        Node repetido = new Node("A", "Otro", NodeType.SHELTER, "Sur", 1, 1);
        assertThrows(IllegalArgumentException.class, () -> graph.addNode(repetido));
    }

    @Test
    void conectaDosLugaresEnAmbosSentidos() {
        graph.connect("A", "B");

        assertEquals(1, graph.getEdges("A").size());
        assertEquals(1, graph.getEdges("B").size());
        assertEquals(0, graph.getEdges("C").size());
        assertEquals(1, graph.edgeCount());
    }

    @Test
    void noPermiteConexionesRepetidas() {
        graph.connect("A", "B");
        assertThrows(IllegalArgumentException.class, () -> graph.connect("B", "A"));
    }

    @Test
    void noPermiteConectarLugaresQueNoExisten() {
        assertThrows(IllegalArgumentException.class, () -> graph.connect("A", "Z"));
    }

    @Test
    void eliminaUnaConexion() {
        graph.connect("A", "B");
        graph.connect("B", "C");

        assertTrue(graph.disconnect("B", "A"));
        assertEquals(0, graph.getEdges("A").size());
        assertEquals(1, graph.getEdges("B").size());
        assertEquals(1, graph.edgeCount());
        assertFalse(graph.disconnect("A", "B"));
    }

    @Test
    void sabeLasConexionesDeCadaLugar() {
        graph.connect("A", "B");
        graph.connect("B", "C");

        List<Edge> deB = graph.getEdges("B");
        assertEquals(2, deB.size());
        Node vecino = deB.get(0).other(graph.getNode("B"));
        assertEquals("A", vecino.getId());
    }
}
