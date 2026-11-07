package pathfindead.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NodeTest {

    @Test
    void guardaSusDatos() {
        Node parque = new Node("C-01", "Parque Principal", NodeType.CONTROL_CENTER, "Central", 6, 7);

        assertEquals("C-01", parque.getId());
        assertEquals("Parque Principal", parque.getName());
        assertEquals(NodeType.CONTROL_CENTER, parque.getType());
        assertEquals("Central", parque.getZone());
    }

    @Test
    void dosNodosConElMismoIdSonIguales() {
        Node a = new Node("R-01", "Barrio Colonial", NodeType.SHELTER, "Central", 9, 4);
        Node b = new Node("R-01", "Otro nombre", NodeType.SHELTER, "Central", 0, 0);

        assertEquals(a, b);
    }
}