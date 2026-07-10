package com.example.enterpriseraglab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VectorDB {
    private final Map<Integer, List<Document>> clusters = new HashMap<>();

    public void insert(Document document) {
        clusters.computeIfAbsent(document.getCluster(), key -> new ArrayList<>())
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

        for (Integer cluster : clusters.keySet()) {
            double score = Similarity.cosine(queryVector, centroid(cluster));

            if (score > bestScore) {
                bestScore = score;
                nearestCluster = cluster;
            }
        }

        return nearestCluster;
    }

    public double[] centroid(int cluster) {
        List<Document> documents = clusters.get(cluster);

        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("Cluster does not exist.");
        }

        int dimension = documents.get(0).getVector().length;
        double[] centroid = new double[dimension];

        for (Document document : documents) {
            double[] vector = document.getVector();

            if (vector.length != dimension) {
                throw new IllegalArgumentException("All vectors in a cluster must have the same dimension.");
            }

            for (int i = 0; i < dimension; i++) {
                centroid[i] += vector[i];
            }
        }

        for (int i = 0; i < dimension; i++) {
            centroid[i] /= documents.size();
        }

        return centroid;
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
