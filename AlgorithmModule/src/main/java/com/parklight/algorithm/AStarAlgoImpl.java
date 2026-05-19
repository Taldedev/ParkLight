package com.parklight.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * A* shortest path algorithm.
 * Like Dijkstra, but uses a heuristic h(node, goal) to guide the search
 * toward the destination. With an admissible heuristic (one that never
 * overestimates the real remaining distance) the result is optimal.
 *
 * @param <T> the type of graph nodes
 */
public class AStarAlgoImpl<T> extends AbstractAlgoShortestPath<T> {

    private final BiFunction<T, T, Double> heuristic;

    // A* with a custom heuristic h(current, goal).
    public AStarAlgoImpl(BiFunction<T, T, Double> heuristic) {
        if (heuristic == null) {
            throw new IllegalArgumentException("Heuristic cannot be null");
        }
        this.heuristic = heuristic;
    }

    // A* with a zero heuristic; effectively behaves like Dijkstra.
    public AStarAlgoImpl() {
        this((a, b) -> 0.0);
    }

    @Override
    public List<T> findShortestPath(T source, T destination) {
        validate(source, destination);
        Map<T, T> prev = new HashMap<>();
        runAStar(source, destination, prev);
        return buildPath(prev, source, destination);
    }

    @Override
    public double getDistance(T source, T destination) {
        validate(source, destination);
        Map<T, Double> g = runAStar(source, destination, null);
        return g.getOrDefault(destination, Double.POSITIVE_INFINITY);
    }

    // Runs the A* search from source toward destination.
    // `g` holds the best known cost from source to each visited node.
    // The priority queue is ordered by f = g + h.
    private Map<T, Double> runAStar(T source, T destination, Map<T, T> predecessors) {
        Map<T, Double> g = new HashMap<>();
        Set<T> closed = new HashSet<>();
        PriorityQueue<Pair> open = new PriorityQueue<>();

        g.put(source, 0.0);
        open.offer(new Pair(source, h(source, destination)));

        while (!open.isEmpty()) {
            Pair cur = open.poll();
            T u = cur.node;
            if (!closed.add(u)) {
                continue; // stale entry, already finalized
            }
            if (u.equals(destination)) {
                return g;
            }

            double gu = g.get(u);
            for (Map.Entry<T, Double> e : neighborsOf(u).entrySet()) {
                T v = e.getKey();
                if (closed.contains(v)) {
                    continue;
                }
                double tentative = gu + e.getValue();
                if (tentative < g.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    g.put(v, tentative);
                    if (predecessors != null) {
                        predecessors.put(v, u);
                    }
                    open.offer(new Pair(v, tentative + h(v, destination)));
                }
            }
        }
        return g;
    }

    // Safe call to the user heuristic; treats negative or null values as zero.
    private double h(T current, T goal) {
        Double value = heuristic.apply(current, goal);
        if (value == null || value < 0) {
            return 0;
        }
        return value;
    }

    private void validate(T source, T destination) {
        if (source == null || destination == null) {
            throw new IllegalArgumentException("Source and destination cannot be null");
        }
    }

    // (node, f-score) pair used inside the priority queue.
    private class Pair implements Comparable<Pair> {
        final T node;
        final double f;

        Pair(T node, double f) {
            this.node = node;
            this.f = f;
        }

        public int compareTo(Pair other) {
            return Double.compare(this.f, other.f);
        }
    }
}
