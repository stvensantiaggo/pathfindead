package pathfindead.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CostFormulaTest {

    @Test
    void sinPeligrosElCostoEsLaDistancia() {
        assertEquals(10.0, CostFormula.calculate(10.0, 0, 0, 0), 1e-9);
    }

    @Test
    void cadaPeligroAumentaElCosto() {
        // 10 × (1 + 0.20·5) = 20
        assertEquals(20.0, CostFormula.calculate(10.0, 5, 0, 0), 1e-9);
        // 10 × (1 + 0.15·2) = 13
        assertEquals(13.0, CostFormula.calculate(10.0, 0, 2, 0), 1e-9);
        // 10 × (1 + 0.05·4) = 12
        assertEquals(12.0, CostFormula.calculate(10.0, 0, 0, 4), 1e-9);
    }

    @Test
    void conTodoAlMaximoElCostoEsElTripleDeLaDistancia() {
        assertEquals(30.0, CostFormula.calculate(10.0, 5, 5, 5), 1e-9);
    }

    @Test
    void rechazaDistanciaNegativa() {
        assertThrows(IllegalArgumentException.class, () -> CostFormula.calculate(-1.0, 0, 0, 0));
    }

    @Test
    void elCostoDeLasConexionesDeLaCiudadEsAlMenosSuDistancia() {
        Graph city = CityFactory.createCity();
        for (Edge e : city.getAllEdges()) {
            assertTrue(e.getCost() >= e.getDistance(), "Costo menor que la distancia en " + e);
        }
    }
}