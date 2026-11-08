package pathfindead.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EdgeTest {

    // Se toman nodos reales de la ciudad para no depender del constructor de Node
    private final Graph city = CityFactory.createCity();
    private final Node c01 = city.getNode("C-01");
    private final Node r04 = city.getNode("R-04");
    private final Node r01 = city.getNode("R-01");

    // ---------- Fase 3 ----------

    @Test
    void calculaLaDistanciaEuclidiana() {
        Edge e = new Edge(c01, r04);
        assertEquals(2.0, e.getDistance(), 0.01);
    }

    @Test
    void laDistanciaNoDependeDelOrden() {
        assertEquals(new Edge(c01, r01).getDistance(), new Edge(r01, c01).getDistance(), 1e-9);
    }

    @Test
    void otherDevuelveElExtremoContrario() {
        Edge e = new Edge(c01, r04);
        assertEquals(r04, e.other(c01));
        assertEquals(c01, e.other(r04));
        assertThrows(IllegalArgumentException.class, () -> e.other(r01));
    }

    @Test
    void dosConexionesSonIgualesSinImportarElOrden() {
        assertEquals(new Edge(c01, r04), new Edge(r04, c01));
        assertEquals(new Edge(c01, r04).hashCode(), new Edge(r04, c01).hashCode());
    }

    @Test
    void noPermiteConectarUnNodoConsigoMismo() {
        assertThrows(IllegalArgumentException.class, () -> new Edge(c01, c01));
    }

    // ---------- Fase 4: peligros ----------

    @Test
    void losPeligrosEmpiezanEnCero() {
        Edge e = new Edge(c01, r04);
        assertEquals(0, e.getZombieDanger());
        assertEquals(0, e.getHostileDanger());
        assertEquals(0, e.getWindRisk());
    }

    @Test
    void aceptaLosLimitesDeLaEscala() {
        Edge e = new Edge(c01, r04);
        e.setZombieDanger(5);
        e.setHostileDanger(0);
        e.setWindRisk(5);
        assertEquals(5, e.getZombieDanger());
        assertEquals(0, e.getHostileDanger());
        assertEquals(5, e.getWindRisk());
    }

    @Test
    void rechazaNivelesFueraDeRango() {
        Edge e = new Edge(c01, r04);
        assertThrows(IllegalArgumentException.class, () -> e.setZombieDanger(6));
        assertThrows(IllegalArgumentException.class, () -> e.setHostileDanger(-1));
        assertThrows(IllegalArgumentException.class, () -> e.setWindRisk(10));
    }
}