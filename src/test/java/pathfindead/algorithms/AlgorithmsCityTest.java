package pathfindead.algorithms;

import org.junit.jupiter.api.Test;
import pathfindead.model.CityFactory;
import pathfindead.model.Edge;
import pathfindead.model.Graph;
import pathfindead.model.Node;
import pathfindead.model.NodeType;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/** Puntos 51 y 52: comparar Prim con Kruskal y probar los 4 algoritmos en la ciudad. */
class AlgorithmsCityTest {

    // ---------- Punto 51: Prim vs Kruskal ----------

    @Test
    void primYKruskalTienenElMismoCostoEnLaCiudad() {
        Graph city = CityFactory.createCity();
        assertEquals(Prim.compute(city, "C-01").totalCost(),
                Kruskal.compute(city).totalCost(), 1e-9);
    }

    @Test
    void primYKruskalEligenLasMismasConexionesEnLaCiudad() {
        Graph city = CityFactory.createCity();
        // Se comparan como conjuntos porque el orden en que las eligen es distinto
        assertEquals(new HashSet<>(Prim.compute(city, "C-01").edges()),
                new HashSet<>(Kruskal.compute(city).edges()));
    }

    @Test
    void conEmpatesPuedenElegirDistintoPeroCuestanLoMismo() {
        Graph g = new Graph();
        g.addNode(new Node("A", "A", NodeType.SHELTER, "Test", 0, 0));
        g.addNode(new Node("B", "B", NodeType.SHELTER, "Test", 3, 0));
        g.addNode(new Node("C", "C", NodeType.SHELTER, "Test", 3, 3));
        g.addNode(new Node("D", "D", NodeType.SHELTER, "Test", 0, 3));
        g.connect("A", "B");   // todas miden 3: hay varios árboles mínimos posibles
        g.connect("B", "C");
        g.connect("C", "D");
        g.connect("D", "A");
        assertEquals(9.0, Prim.compute(g, "A").totalCost(), 1e-9);
        assertEquals(9.0, Kruskal.compute(g).totalCost(), 1e-9);
    }

    @Test
    void siguenCoincidiendoCuandoCambianLosPeligros() {
        Graph city = CityFactory.createCity();
        // Una horda máxima en los dos corredores más baratos
        for (Edge e : city.getAllEdges()) {
            String s = e.toString();
            if ((s.contains("C-01") && s.contains("R-04")) || (s.contains("R-01") && s.contains("R-02"))) {
                e.updateHazards(5, 5, 5);
            }
        }
        assertEquals(Prim.compute(city, "C-01").totalCost(), Kruskal.compute(city).totalCost(), 1e-9);

        AllPairsResult fw = FloydWarshall.compute(city);
        for (Node a : city.getNodes()) {
            for (Node b : city.getNodes()) {
                assertEquals(Dijkstra.shortestPath(city, a.getId(), b.getId()).totalCost(),
                        fw.cost(a.getId(), b.getId()), 1e-9);
            }
        }
    }

    // ---------- Punto 52: los 4 algoritmos con la ciudad completa ----------

    @Test
    void losCuatroAlgoritmosFuncionanEnLaCiudad() {
        Graph city = CityFactory.createCity();

        // Dijkstra: hay ruta entre cualquier par de lugares
        for (Node a : city.getNodes()) {
            for (Node b : city.getNodes()) {
                assertTrue(Dijkstra.shortestPath(city, a.getId(), b.getId()).isFound());
            }
        }

        // Floyd-Warshall: ningún costo infinito
        AllPairsResult fw = FloydWarshall.compute(city);
        for (Node a : city.getNodes()) {
            for (Node b : city.getNodes()) {
                assertFalse(Double.isInfinite(fw.cost(a.getId(), b.getId())));
            }
        }

        // Prim y Kruskal: conectan toda la ciudad
        assertTrue(Prim.compute(city, "C-01").spans(city));
        assertTrue(Kruskal.compute(city).spans(city));
    }
}