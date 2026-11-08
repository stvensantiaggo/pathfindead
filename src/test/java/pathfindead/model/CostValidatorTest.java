package pathfindead.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CostValidatorTest {

    private final Graph city = CityFactory.createCity();

    @Test
    void laCiudadInicialTieneTodosLosCostosValidos() {
        assertTrue(CostValidator.allCostsValid(city));
        assertTrue(CostValidator.findInvalid(city).isEmpty());
        assertDoesNotThrow(() -> CostValidator.validate(city));
    }

    @Test
    void ceroEsUnCostoValido() {
        assertTrue(CostValidator.isValid(0.0));
        assertEquals(0.0, CostFormula.calculate(0.0, 5, 5, 5), 1e-9);
    }

    @Test
    void negativosYNaNSonInvalidos() {
        assertFalse(CostValidator.isValid(-0.01));
        assertFalse(CostValidator.isValid(Double.NaN));
    }

    @Test
    void cualquierCombinacionDePeligrosDaCostoValido() {
        // Prueba las 216 combinaciones posibles (6 × 6 × 6) en todas las conexiones
        for (Edge e : city.getAllEdges()) {
            for (int z = 0; z <= 5; z++) {
                for (int h = 0; h <= 5; h++) {
                    for (int w = 0; w <= 5; w++) {
                        e.updateHazards(z, h, w);
                        assertTrue(e.getCurrentCost() >= 0, "Costo negativo en " + e);
                        assertTrue(e.getCurrentCost() >= e.getDistance(), "Costo menor que la distancia en " + e);
                    }
                }
            }
            e.resetToOriginal();
        }
        assertTrue(CostValidator.allCostsValid(city));
    }
}