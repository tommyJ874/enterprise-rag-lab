package com.example.enterpriseraglab;

public class Similarity {

    private Similarity() {
    }

    public static double cosine(double[] a, double[] b) {
        validateSameDimension(a, b);

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static void validateSameDimension(double[] a, double[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Vectors must not be null.");
        }

        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors must have the same dimension.");
        }
    }
}
