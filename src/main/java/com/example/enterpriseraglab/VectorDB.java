package com.example.enterpriseraglab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VectorDB {
    private final List<Document> documents = new ArrayList<>();

    public void insert(Document document) {
        documents.add(document);
    }

    public List<SearchResult> search(double[] queryVector) {
        List<SearchResult> results = new ArrayList<>();

        for (Document document : documents) {
            double score = Similarity.cosine(queryVector, document.getVector());
            results.add(new SearchResult(document, score));
        }

        results.sort(Comparator.comparing(SearchResult::getScore).reversed());

        return results;
    }

    public List<SearchResult> search(double[] queryVector, int topK) {
        if (topK < 0) {
            throw new IllegalArgumentException("topK must be greater than or equal to 0.");
        }

        return search(queryVector).stream()
                .limit(topK)
                .toList();
    }

    public int size() {
        return documents.size();
    }
}
