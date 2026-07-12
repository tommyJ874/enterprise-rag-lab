package com.example.enterpriseraglab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class KMeans {
    private final int k;
    private final Random random;

    public KMeans(int k) {
        this(k, new Random());
    }

    public KMeans(int k, Random random) {
        if (k < 1) {
            throw new IllegalArgumentException("k must be greater than 0.");
        }

        if (random == null) {
            throw new IllegalArgumentException("random must not be null.");
        }

        this.k = k;
        this.random = random;
    }

    public List<Centroid> initialize(List<Document> docs) {
        if (docs == null) {
            throw new IllegalArgumentException("docs must not be null.");
        }

        if (docs.size() < k) {
            throw new IllegalArgumentException("docs size must be greater than or equal to k.");
        }

        List<Document> shuffled = new ArrayList<>(docs);
        Collections.shuffle(shuffled, random);

        return shuffled.stream()
                .limit(k)
                .map(document -> new Centroid(document.getVector().clone()))
                .toList();
    }

    public Map<Centroid, List<Document>> assign(List<Document> docs, List<Centroid> centroids) {
        if (docs == null || centroids == null) {
            throw new IllegalArgumentException("docs and centroids must not be null.");
        }

        if (centroids.isEmpty()) {
            throw new IllegalArgumentException("centroids must not be empty.");
        }

        Map<Centroid, List<Document>> assignments = new LinkedHashMap<>();

        for (Centroid centroid : centroids) {
            assignments.put(centroid, new ArrayList<>());
        }

        for (Document document : docs) {
            Centroid nearest = nearestCentroid(document, centroids);
            assignments.get(nearest).add(document);
        }

        return assignments;
    }

    private Centroid nearestCentroid(Document document, List<Centroid> centroids) {
        Centroid nearest = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Centroid centroid : centroids) {
            double score = Similarity.cosine(document.getVector(), centroid.getVector());

            if (score > bestScore) {
                bestScore = score;
                nearest = centroid;
            }
        }

        return nearest;
    }
}
