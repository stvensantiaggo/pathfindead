package pathfindead.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCostTest {

    private final Graph city = CityFactory.createCity();
    private final Node c01 = city.getNode("C-01");
    private final Node r04 = city.getNode("R-04");

    @Test
    void unaConexionNuevaTieneCostoIgualALaDistancia() {
        Edge e = new Edge(c01, r04);
        assertEquals(e.getDistance(), e.getOriginalCost(), 1e-9);
        assertEquals(e.getDistance(), e.getCurrentCost(), 1e-9);
    }

    @Test
    void cambiarUnPeligroActualizaElCostoActualPeroNoElOriginal() {
        Edge e = new Edge(c01, r04);          // distancia 2.0
        e.setZombieDanger(5);                 // 2 × (1 + 1.0) = 4
        assertEquals(4.0, e.getCurrentCost(), 1e-9);
        assertEquals(2.0, e.getOriginalCost(), 1e-9);
    }

    @Test
    void updateHazardsCambiaLosTresPeligros() {
        Edge e = new Edge(c01, r04);
        e.updateHazards(5, 1, 0);             // 2 × (1 + 1.0 + 0.15) = 4.3
        assertEquals(5, e.getZombieDanger());
        assertEquals(1, e.getHostileDanger());
        assertEquals(0, e.getWindRisk());
        assertEquals(4.3, e.getCurrentCost(), 1e-9);
    }

    @Test
    void updateHazardsInvalidoNoCambiaNada() {
        Edge e = new Edge(c01, r04);
        e.updateHazards(2, 2, 2);
        double antes = e.getCurrentCost();
        assertThrows(IllegalArgumentException.class, () -> e.updateHazards(3, 9, 1));
        assertEquals(2, e.getZombieDanger());
        assertEquals(antes, e.getCurrentCost(), 1e-9);
    }

    @Test
    void resetToOriginalRestauraPeligrosYCosto() {
        Edge e = new Edge(c01, r04);
        e.updateHazards(1, 1, 1);
        e.saveAsOriginal();
        double original = e.getCurrentCost();

        e.updateHazards(5, 5, 5);
        assertNotEquals(original, e.getCurrentCost());

        e.resetToOriginal();
        assertEquals(original, e.getCurrentCost(), 1e-9);
        assertEquals(1, e.getZombieDanger());
    }

    @Test
    void enLaCiudadInicialElCostoActualEsIgualAlOriginal() {
        for (Edge e : city.getAllEdges()) {
            assertEquals(e.getOriginalCost(), e.getCurrentCost(), 1e-9, "Diferencia en " + e);
        }
    }

    @Test
    void elCostoOriginalIncluyeLosPeligrosIniciales() {
        for (Edge e : city.getAllEdges()) {
            if (e.toString().contains("Z-02") && e.toString().contains("Z-03")) {
                // 7.07 × (1 + 0.20·5 + 0.15·0 + 0.05·2) ≈ 14.85
                assertEquals(14.85, e.getOriginalCost(), 0.01);
            }
        }
    }
}