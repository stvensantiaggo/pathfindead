package pathfindead.simulation;

import org.junit.jupiter.api.Test;
import pathfindead.model.CityFactory;
import pathfindead.model.Graph;

import static org.junit.jupiter.api.Assertions.*;

class SupplyStateTest {

    private final Graph city = CityFactory.createCity();
    private final SupplyState supplies = new SupplyState(city, 4);

    @Test
    void hayUnEstadoPorCadaRefugio() {
        assertEquals(6, supplies.all().size());
        assertTrue(supplies.isShelter(city.getNode("R-01")));
        assertFalse(supplies.isShelter(city.getNode("C-01")));
        assertNull(supplies.get(city.getNode("H-01")));
    }

    @Test
    void unRefugioNoAceptaMasDeLoQueNecesita() {
        Shelter s = supplies.get(city.getNode("R-03"));
        assertEquals(3, s.receive(3));
        assertEquals(1, s.receive(5));   // solo le faltaba 1
        assertTrue(s.isSupplied());
        assertEquals(0, s.remaining());
    }

    @Test
    void cuentaLosRefugiosAbastecidos() {
        assertEquals(0, supplies.suppliedCount());
        for (Shelter s : supplies.all()) {
            s.receive(4);
        }
        assertTrue(supplies.allSupplied());
    }

    @Test
    void rechazaNecesidadesYCantidadesInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> new Shelter(city.getNode("R-01"), 0));
        Shelter s = supplies.get(city.getNode("R-01"));
        assertThrows(IllegalArgumentException.class, () -> s.receive(-1));
    }
}