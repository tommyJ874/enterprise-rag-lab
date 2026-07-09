package com.example.enterpriseraglab;

public class Document {
    private final String id;
    private final String text;
    private final double[] vector;
    private final int cluster;

    public Document(String id, String text, double[] vector) {
        this(id, text, vector, 0);
    }

    public Document(String id, String text, double[] vector, int cluster) {
        this.id = id;
        this.text = text;
        this.vector = vector;
        this.cluster = cluster;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public double[] getVector() {
        return vector;
    }

    public int getCluster() {
        return cluster;
    }
}
