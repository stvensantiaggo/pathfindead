package pathfindead.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EdgeTest {

    private final Node a = new Node("A", "Lugar A", NodeType.SHELTER, "Central", 0, 0);
    private final Node b = new Node("B", "Lugar B", NodeType.SHELTER, "Central", 3, 4);
    private final Node c = new Node("C", "Lugar C", NodeType.SHELTER, "Central", 6, 8);

    @Test
    void calculaLaDistanciaEntreLosDosLugares() {
        Edge edge = new Edge(a, b);
        assertEquals(5.0, edge.getDistance(), 0.0001);
    }

    @Test
    void devuelveElExtremoContrario() {
        Edge edge = new Edge(a, b);
        assertEquals(b, edge.other(a));
        assertEquals(a, edge.other(b));
    }

    @Test
    void noImportaElOrdenDeLosExtremos() {
        assertEquals(new Edge(a, b), new Edge(b, a));
    }

    @Test
    void noPermiteUnirUnLugarConSiMismo() {
        assertThrows(IllegalArgumentException.class, () -> new Edge(a, a));
    }

    @Test
    void fallaSiElNodoNoPerteneceALaConexion() {
        Edge edge = new Edge(a, b);
        assertThrows(IllegalArgumentException.class, () -> edge.other(c));
    }
}
