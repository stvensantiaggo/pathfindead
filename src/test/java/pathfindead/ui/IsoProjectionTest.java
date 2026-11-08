package pathfindead.ui;

import org.junit.jupiter.api.Test;
import pathfindead.model.CityFactory;
import pathfindead.model.Graph;
import pathfindead.model.Node;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Punto 65: la posición visual de cada ubicación corresponde con su posición lógica en el grafo. */
class IsoProjectionTest {

    private final IsoProjection iso = IsoProjection.forCity();
    private final List<Node> nodes = List.copyOf(CityFactory.createCity().getNodes());

    @Test
    void laCasillaCeroCaeEnElOrigen() {
        assertEquals(IsoProjection.CITY_ORIGIN_X, iso.toScreenX(0, 0), 1e-9);
        assertEquals(IsoProjection.CITY_ORIGIN_Y, iso.toScreenY(0, 0), 1e-9);
    }

    @Test
    void unPasoEnXyEnYSeVeEnDiagonal() {
        // +1 en X: 32 px a la derecha y 16 abajo; +1 en Y: 32 px a la izquierda y 16 abajo
        assertEquals(32, iso.toScreenX(1, 0) - iso.toScreenX(0, 0), 1e-9);
        assertEquals(16, iso.toScreenY(1, 0) - iso.toScreenY(0, 0), 1e-9);
        assertEquals(-32, iso.toScreenX(0, 1) - iso.toScreenX(0, 0), 1e-9);
        assertEquals(16, iso.toScreenY(0, 1) - iso.toScreenY(0, 0), 1e-9);
    }

    @Test
    void cadaNodoVuelveASuCoordenadaLogica() {
        // Pantalla -> cuadrícula debe devolver exactamente la (x, y) del grafo
        for (Node n : nodes) {
            double sx = iso.toScreenX(n.getX(), n.getY());
            double sy = iso.toScreenY(n.getX(), n.getY());
            assertEquals(n.getX(), iso.toGridX(sx, sy), 1e-9, "X distinta en " + n.getId());
            assertEquals(n.getY(), iso.toGridY(sx, sy), 1e-9, "Y distinta en " + n.getId());
        }
    }

    @Test
    void todosLosNodosCabenEnElMapa() {
        // Diseño de 1100 × 700; el marcador vuela 60 px arriba y su ID ocupa 24 px más
        for (Node n : nodes) {
            double sx = iso.toScreenX(n.getX(), n.getY());
            double sy = iso.toScreenY(n.getX(), n.getY());
            assertTrue(sx - 16 >= 0 && sx + 16 <= 1100, "Fuera del mapa (X): " + n.getId());
            assertTrue(sy - 60 - 24 >= 0 && sy <= 700, "Fuera del mapa (Y): " + n.getId());
        }
    }

    @Test
    void ningunNodoSeEncimaConOtro() {
        for (Node a : nodes) {
            for (Node b : nodes) {
                if (a.equals(b)) continue;
                double d = Math.hypot(
                        iso.toScreenX(a.getX(), a.getY()) - iso.toScreenX(b.getX(), b.getY()),
                        iso.toScreenY(a.getX(), a.getY()) - iso.toScreenY(b.getX(), b.getY()));
                assertTrue(d >= 30, a.getId() + " y " + b.getId() + " están demasiado cerca");
            }
        }
    }

    @Test
    void elOrdenEnPantallaRespetaElOrdenLogico() {
        for (Node a : nodes) {
            for (Node b : nodes) {
                // Más al fondo (x + y menor) => más arriba en pantalla
                if (a.getX() + a.getY() < b.getX() + b.getY()) {
                    assertTrue(iso.toScreenY(a.getX(), a.getY()) < iso.toScreenY(b.getX(), b.getY()));
                }
                // Mayor (x - y) => más a la derecha en pantalla
                if (a.getX() - a.getY() > b.getX() - b.getY()) {
                    assertTrue(iso.toScreenX(a.getX(), a.getY()) > iso.toScreenX(b.getX(), b.getY()));
                }
            }
        }
    }
}