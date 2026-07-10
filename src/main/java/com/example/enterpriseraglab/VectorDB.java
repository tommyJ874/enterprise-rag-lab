package com.example.enterpriseraglab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VectorDB {
    private final Map<Integer, List<Document>> clusters = new HashMap<>();
    private final Map<Integer, double[]> centroids = new HashMap<>();
    private final Map<Integer, Integer> clusterCounts = new HashMap<>();

    public void insert(Document document) {
        int cluster = document.getCluster();
        double[] vector = document.getVector();

        if (vector == null) {
            throw new IllegalArgumentException("Vector must not be null.");
        }

        if (!centroids.containsKey(cluster)) {
            centroids.put(cluster, vector.clone());
            clusterCounts.put(cluster, 1);
        } else {
            double[] centroid = centroids.get(cluster);
            int count = clusterCounts.get(cluster);

            updateCentroid(centroid, vector, count);
            clusterCounts.put(cluster, count + 1);
        }

        clusters.computeIfAbsent(cluster, key -> new ArrayList<>())
                .add(document);
    }

    public List<SearchResult> search(double[] queryVector, int queryCluster) {
        return rank(queryVector, clusters.getOrDefault(queryCluster, List.of()));
    }

    public List<SearchResult> search(double[] queryVector, int queryCluster, int topK) {
        validateTopK(topK);

        return search(queryVector, queryCluster).stream()
                .limit(topK)
                .toList();
    }

    public List<SearchResult> searchNearestCluster(double[] queryVector) {
        if (clusters.isEmpty()) {
            return List.of();
        }

        int nearestCluster = nearestCluster(queryVector);

        return search(queryVector, nearestCluster);
    }

    public List<SearchResult> searchNearestCluster(double[] queryVector, int topK) {
        validateTopK(topK);

        return searchNearestCluster(queryVector).stream()
                .limit(topK)
                .toList();
    }

    public int nearestCluster(double[] queryVector) {
        if (clusters.isEmpty()) {
            throw new IllegalStateException("No clusters exist.");
        }

        int nearestCluster = -1;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Map.Entry<Integer, double[]> entry : centroids.entrySet()) {
            double score = Similarity.cosine(queryVector, entry.getValue());

            if (score > bestScore) {
                bestScore = score;
                nearestCluster = entry.getKey();
            }
        }

        return nearestCluster;
    }

    public double[] centroid(int cluster) {
        double[] centroid = centroids.get(cluster);

        if (centroid == null) {
            throw new IllegalArgumentException("Cluster does not exist.");
        }

        return centroid.clone();
    }

    public static void updateCentroid(double[] centroid, double[] newVector, int count) {
        if (count < 1) {
            throw new IllegalArgumentException("count must be greater than 0.");
        }

        if (centroid == null || newVector == null) {
            throw new IllegalArgumentException("Vectors must not be null.");
        }

        if (centroid.length != newVector.length) {
            throw new IllegalArgumentException("Vectors must have the same dimension.");
        }

        for (int i = 0; i < centroid.length; i++) {
            centroid[i] = (centroid[i] * count + newVector[i]) / (count + 1);
        }
    }

    public List<SearchResult> bruteForceSearch(double[] queryVector) {
        List<Document> documents = clusters.values().stream()
                .flatMap(List::stream)
                .toList();

        return rank(queryVector, documents);
    }

    public List<SearchResult> bruteForceSearch(double[] queryVector, int topK) {
        validateTopK(topK);

        return bruteForceSearch(queryVector).stream()
                .limit(topK)
                .toList();
    }

    public int size() {
        return clusters.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    public int clusterSize(int cluster) {
        return clusters.getOrDefault(cluster, List.of()).size();
    }

    private List<SearchResult> rank(double[] queryVector, List<Document> documents) {
        List<SearchResult> results = new ArrayList<>();

        for (Document document : documents) {
            double score = Similarity.cosine(queryVector, document.getVector());
            results.add(new SearchResult(document, score));
        }

        results.sort(Comparator.comparing(SearchResult::getScore).reversed());

        return results;
    }

    private void validateTopK(int topK) {
        if (topK < 0) {
            throw new IllegalArgumentException("topK must be greater than or equal to 0.");
        }
    }
}
