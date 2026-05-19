package com.parklight.algorithm;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Common base class for shortest path implementations.
 * Holds the graph and shares helper code between concrete algorithms.
 *
 * @param <T> the type of graph nodes
 */
public abstract class AbstractAlgoShortestPath<T> implements IAlgoShortestPath<T> {

    // Adjacency list: node -> (neighbor -> edge weight)
    protected final Map<T, Map<T, Double>> graph = new HashMap<>();

    @Override
    public void addEdge(T from, T to, double weight) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Nodes cannot be null");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("Edge weight must be non-negative");
        }
        graph.computeIfAbsent(from, k -> new HashMap<>());
        graph.computeIfAbsent(to, k -> new HashMap<>());
        graph.get(from).put(to, weight);
    }

    // Returns the outgoing neighbors of a node (empty map if node is unknown).
    protected Map<T, Double> neighborsOf(T node) {
        return graph.getOrDefault(node, Collections.emptyMap());
    }

    // Rebuilds the path source -> ... -> destination from a predecessor map.
    // Returns an empty list if destination is unreachable.
    protected List<T> buildPath(Map<T, T> predecessors, T source, T destination) {
        if (source.equals(destination)) {
            return Collections.singletonList(source);
        }
        if (!predecessors.containsKey(destination)) {
            return Collections.emptyList();
        }
        LinkedList<T> path = new LinkedList<>();
        T cur = destination;
        while (cur != null) {
            path.addFirst(cur);
            if (cur.equals(source)) {
                return path;
            }
            cur = predecessors.get(cur);
        }
        return Collections.emptyList();
    }
}
