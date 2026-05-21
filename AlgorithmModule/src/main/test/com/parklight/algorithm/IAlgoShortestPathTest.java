package com.parklight.algorithm;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the shortest path algorithm family.
 */
public class IAlgoShortestPathTest {

    private IAlgoShortestPath<String> dijkstra;
    private IAlgoShortestPath<String> astar;

    @Before
    public void setUp() {
        dijkstra = new DijkstraAlgoImpl<>();
        astar = new AStarAlgoImpl<>();
    }

    // Builds a small directed weighted graph.
    // Best A -> D is A -> B -> D, total cost 3.
    private void buildGraph(IAlgoShortestPath<String> algo) {
        algo.addEdge("A", "B", 1);
        algo.addEdge("A", "C", 4);
        algo.addEdge("B", "C", 1);
        algo.addEdge("B", "D", 2);
        algo.addEdge("C", "D", 3);
    }

    // ----- Dijkstra -----

    @Test
    public void dijkstraFindsShortestPath() {
        buildGraph(dijkstra);
        List<String> path = dijkstra.findShortestPath("A", "D");
        assertEquals(Arrays.asList("A", "B", "D"), path);
        assertEquals(3.0, dijkstra.getDistance("A", "D"), 1e-9);
    }

    @Test
    public void dijkstraHandlesUnreachableTarget() {
        dijkstra.addEdge("A", "B", 1);
        dijkstra.addEdge("X", "Y", 1);
        assertTrue(dijkstra.findShortestPath("A", "Y").isEmpty());
        assertEquals(Double.POSITIVE_INFINITY, dijkstra.getDistance("A", "Y"), 1e-9);
    }

    // ----- A* -----

    @Test
    public void astarFindsShortestPath() {
        buildGraph(astar);
        List<String> path = astar.findShortestPath("A", "D");
        assertEquals(Arrays.asList("A", "B", "D"), path);
        assertEquals(3.0, astar.getDistance("A", "D"), 1e-9);
    }

    @Test
    public void astarWithEuclideanHeuristicStaysOptimal() {
        Map<String, double[]> coords = new HashMap<>();
        coords.put("A", new double[]{0, 0});
        coords.put("B", new double[]{1, 0});
        coords.put("C", new double[]{0, 2});
        coords.put("D", new double[]{2, 0});

        BiFunction<String, String, Double> euclidean = (from, to) -> {
            double[] f = coords.get(from);
            double[] t = coords.get(to);
            double dx = f[0] - t[0];
            double dy = f[1] - t[1];
            return Math.sqrt(dx * dx + dy * dy);
        };

        IAlgoShortestPath<String> algo = new AStarAlgoImpl<>(euclidean);
        buildGraph(algo);

        assertEquals(3.0, algo.getDistance("A", "D"), 1e-9);
        assertEquals(Arrays.asList("A", "B", "D"), algo.findShortestPath("A", "D"));
    }
}
