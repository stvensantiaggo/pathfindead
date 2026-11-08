package pathfindead.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CityFactoryTest {

    private final Graph city = CityFactory.createCity();

    @Test
    void laCiudadTiene14LugaresY24Conexiones() {
        assertEquals(14, city.nodeCount());
        assertEquals(24, city.edgeCount());
    }

    @Test
    void todosLosLugaresEstanConectados() {
        assertTrue(city.isConnected());
    }

    @Test
    void elCentroDeControlTiene8Conexiones() {
        assertEquals(8, city.getEdges("C-01").size());
    }

    @Test
    void sinElCentroDeControlElRestoSigueConectado() {
        // Se copia la lista porque al eliminar conexiones la lista original va cambiando
        for (Edge edge : List.copyOf(city.getEdges("C-01"))) {
            city.disconnect(edge.getA().getId(), edge.getB().getId());
        }
        // Desde R-01 se llega a los otros 12 lugares más él mismo: todos menos C-01
        assertEquals(13, city.reachableFrom("R-01").size());
    }
}
