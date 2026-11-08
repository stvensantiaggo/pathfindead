package pathfindead.algorithms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnionFindTest {

    private UnionFind uf;

    @BeforeEach
    void crear() {
        uf = new UnionFind();
        for (String id : new String[]{"A", "B", "C", "D"}) {
            uf.add(id);
        }
    }

    @Test
    void alInicioCadaLugarEstaSolo() {
        assertFalse(uf.connected("A", "B"));
        assertEquals("A", uf.find("A"));
    }

    @Test
    void unionJuntaDosGrupos() {
        assertTrue(uf.union("A", "B"));
        assertTrue(uf.connected("A", "B"));
        assertFalse(uf.connected("A", "C"));
    }

    @Test
    void unirElMismoGrupoDosVecesDevuelveFalse() {
        uf.union("A", "B");
        uf.union("B", "C");
        assertFalse(uf.union("A", "C"));   // formaría un ciclo
        assertTrue(uf.connected("A", "C"));
    }

    @Test
    void rechazaLugarNoRegistrado() {
        assertThrows(IllegalArgumentException.class, () -> uf.find("X"));
    }
}