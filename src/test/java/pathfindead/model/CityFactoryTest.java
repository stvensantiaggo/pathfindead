package pathfindead.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void todasLasDistanciasSonPositivas() {
        for (Edge e : city.getAllEdges()) {
            assertTrue(e.getDistance() > 0, "Distancia inválida en " + e);
        }
    }

    // ---------- Peligro zombi (punto 33) ----------

    @Test
    void peligroZombiDentroDelRango() {
        for (Edge e : city.getAllEdges()) {
            int d = e.getZombieDanger();
            assertTrue(d >= 0 && d <= 5, "Peligro fuera de rango en " + e);
        }
    }

    @Test
    void corredorEntreHordasEsElMasPeligroso() {
        for (Edge e : city.getAllEdges()) {
            if (e.toString().contains("Z-02") && e.toString().contains("Z-03")) {
                assertEquals(5, e.getZombieDanger());
            }
        }
    }

    @Test
    void rechazaPeligroInvalido() {
        Edge e = city.getAllEdges().iterator().next();
        assertThrows(IllegalArgumentException.class, () -> e.setZombieDanger(6));
        assertThrows(IllegalArgumentException.class, () -> e.setZombieDanger(-1));
    }

    // ---------- Peligro hostil (punto 34) ----------

    @Test
    void peligroHostilDentroDelRango() {
        for (Edge e : city.getAllEdges()) {
            int d = e.getHostileDanger();
            assertTrue(d >= 0 && d <= 5, "Peligro hostil fuera de rango en " + e);
        }
    }

    @Test
    void hospitalesTienenPeligroHostilAlto() {
        for (Edge e : city.getAllEdges()) {
            if (e.toString().contains("R-02") && e.toString().contains("H-01")) {
                assertEquals(3, e.getHostileDanger());
            }
        }
    }

    @Test
    void rechazaPeligroHostilInvalido() {
        Edge e = city.getAllEdges().iterator().next();
        assertThrows(IllegalArgumentException.class, () -> e.setHostileDanger(6));
        assertThrows(IllegalArgumentException.class, () -> e.setHostileDanger(-1));
    }
}