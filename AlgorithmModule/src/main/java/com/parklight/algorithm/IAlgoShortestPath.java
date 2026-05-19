package com.parklight.algorithm;

import java.util.List;

/**
 * Shortest path algorithms over a weighted directed graph.
 *
 * @param <T> the type of graph nodes
 * @author Tal Almagor
 */
public interface IAlgoShortestPath<T> {

    // Adds a directed edge from -> to with a non-negative weight.
    // Endpoints are added automatically if they don't exist yet.
    void addEdge(T from, T to, double weight);

    // Returns the cheapest path from source to destination,
    // or an empty list if no such path exists.
    List<T> findShortestPath(T source, T destination);

    // Returns the cost of the cheapest path,
    // or Double.POSITIVE_INFINITY if no such path exists.
    double getDistance(T source, T destination);
}
