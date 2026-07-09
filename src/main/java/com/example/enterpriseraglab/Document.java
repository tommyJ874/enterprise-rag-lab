package com.example.enterpriseraglab;

public class Document {
    private final String id;
    private final String text;
    private final double[] vector;

    public Document(String id, String text, double[] vector) {
        this.id = id;
        this.text = text;
        this.vector = vector;
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
}
