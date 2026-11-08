package pathfindead.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pathfindead.model.CityFactory;
import pathfindead.model.Graph;
import pathfindead.model.Node;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DroneTest {

    private Graph city;
    private Drone drone;

    @BeforeEach
    void crear() {
        city = CityFactory.createCity();
        drone = new Drone(city, city.getNode("C-01"), 10);
    }

    private List<Node> path(String... ids) {
        return java.util.Arrays.stream(ids).map(city::getNode).toList();
    }

    @Test
    void empiezaEnLaBaseConBateriaYCargaCompletas() {
        assertEquals("C-01", drone.getCurrent().getId());
        assertEquals(100, drone.getBattery(), 1e-9);
        assertEquals(10, drone.getCargo());
        assertFalse(drone.isMoving());
    }

    @Test
    void rechazaRutasQueNoEmpiezanDondeEsta() {
        assertThrows(IllegalArgumentException.class, () -> drone.assignRoute(path("R-04", "R-05")));
    }

    @Test
    void rechazaRutasConSaltosSinCorredor() {
        // C-01 y Z-02 no están conectados directamente
        assertThrows(IllegalArgumentException.class, () -> drone.assignRoute(path("C-01", "Z-02")));
    }

    @Test
    void avanzaPocoAPocoYLlega() {
        drone.assignRoute(path("C-01", "R-04"));   // distancia 2, costo 2
        assertTrue(drone.isMoving());

        List<Node> llegadas = drone.update(0.4);   // 0.4 s × 2.5 = 1 casilla: va por la mitad
        assertTrue(llegadas.isEmpty());
        assertEquals(0.5, drone.getProgress(), 1e-9);

        llegadas = drone.update(1.0);              // le alcanza para llegar
        assertEquals(List.of(city.getNode("R-04")), llegadas);
        assertEquals("R-04", drone.getCurrent().getId());
        assertFalse(drone.isMoving());
        assertEquals(100 - 2.0 * Drone.BATTERY_PER_COST, drone.getBattery(), 1e-9);
    }

    @Test
    void recorreUnaRutaCompletaYGastaBateria() {
        drone.assignRoute(path("C-01", "R-04", "R-05", "Z-02"));   // ruta de Dijkstra, costo 16.57
        List<Node> llegadas = drone.update(100);                    // tiempo de sobra
        assertEquals(path("R-04", "R-05", "Z-02"), llegadas);
        assertEquals("Z-02", drone.getCurrent().getId());
        assertEquals(100 - 16.57 * Drone.BATTERY_PER_COST, drone.getBattery(), 0.01);
    }

    @Test
    void alVolverALaBaseSeRecarga() {
        drone.assignRoute(path("C-01", "R-04"));
        drone.update(100);
        drone.deliverTo(new Shelter(city.getNode("R-04"), 4));
        drone.assignRoute(path("R-04", "C-01"));
        drone.update(100);
        assertEquals(100, drone.getBattery(), 1e-9);
        assertEquals(10, drone.getCargo());
    }

    @Test
    void entregaSoloLoQueElRefugioNecesita() {
        drone.assignRoute(path("C-01", "R-04"));
        drone.update(100);
        Shelter refugio = new Shelter(city.getNode("R-04"), 4);

        assertEquals(4, drone.deliverTo(refugio));
        assertEquals(6, drone.getCargo());
        assertTrue(refugio.isSupplied());
        assertEquals(0, drone.deliverTo(refugio));   // ya no necesita más
    }

    @Test
    void noPuedeEntregarSiNoEstaEnElRefugio() {
        Shelter lejano = new Shelter(city.getNode("R-05"), 4);
        assertThrows(IllegalStateException.class, () -> drone.deliverTo(lejano));
    }

    @Test
    void calculaSiLeAlcanzaLaBateria() {
        List<Node> ruta = path("C-01", "R-04", "R-05", "Z-02");
        assertEquals(16.57 * Drone.BATTERY_PER_COST, drone.batteryNeededFor(ruta), 0.01);
        assertTrue(drone.canFly(ruta));
    }
}