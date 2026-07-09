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
