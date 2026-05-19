package com.parklight.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Dijkstra's shortest path algorithm.
 * Works on graphs with non-negative edge weights.
 *
 * @param <T> the type of graph nodes
 * @author Tal Almagor
 */
public class DijkstraAlgoImpl<T> extends AbstractAlgoShortestPath<T> {

    @Override
    public List<T> findShortestPath(T source, T destination) {
        validate(source, destination);
        Map<T, T> prev = new HashMap<>();
        runDijkstra(source, destination, prev);
        return buildPath(prev, source, destination);
    }

    @Override
    public double getDistance(T source, T destination) {
        validate(source, destination);
        Map<T, Double> dist = runDijkstra(source, destination, null);
        return dist.getOrDefault(destination, Double.POSITIVE_INFINITY);
    }

    // Runs the Dijkstra search from source toward destination.
    // If `predecessors` is non-null it will be filled along the way so that
    // the caller can later reconstruct the path.
    private Map<T, Double> runDijkstra(T source, T destination, Map<T, T> predecessors) {
        Map<T, Double> dist = new HashMap<>();
        Set<T> settled = new HashSet<>();
        PriorityQueue<Pair> queue = new PriorityQueue<>();

        dist.put(source, 0.0);
        queue.offer(new Pair(source, 0.0));

        while (!queue.isEmpty()) {
            Pair cur = queue.poll();
            T u = cur.node;
            if (!settled.add(u)) {
                continue; // stale entry, already finalized
            }
            if (u.equals(destination)) {
                return dist;
            }

            for (Map.Entry<T, Double> e : neighborsOf(u).entrySet()) {
                T v = e.getKey();
                if (settled.contains(v)) {
                    continue;
                }
                double alt = dist.get(u) + e.getValue();
                if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, alt);
                    if (predecessors != null) {
                        predecessors.put(v, u);
                    }
                    queue.offer(new Pair(v, alt));
                }
            }
        }
        return dist;
    }

    private void validate(T source, T destination) {
        if (source == null || destination == null) {
            throw new IllegalArgumentException("Source and destination cannot be null");
        }
    }

    // (node, distance) pair used inside the priority queue.
    private class Pair implements Comparable<Pair> {
        final T node;
        final double dist;

        Pair(T node, double dist) {
            this.node = node;
            this.dist = dist;
        }

        public int compareTo(Pair other) {
            return Double.compare(this.dist, other.dist);
        }
    }
}
