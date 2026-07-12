package com.example.enterpriseraglab;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KMeansTests {

    @Test
    void initializeSelectsKCentroids() {
        KMeans kMeans = new KMeans(2, new Random(1));
        List<Document> docs = List.of(
                new Document("doc-1", "육아휴직", new double[]{1.0, 0.0}),
                new Document("doc-2", "재무제표", new double[]{0.0, 1.0}),
                new Document("doc-3", "보험청약", new double[]{0.5, 0.5})
        );

        List<Centroid> centroids = kMeans.initialize(docs);

        assertThat(centroids).hasSize(2);
    }

    @Test
    void initializeDoesNotModifyOriginalDocumentOrder() {
        KMeans kMeans = new KMeans(2, new Random(1));
        List<Document> docs = List.of(
                new Document("doc-1", "육아휴직", new double[]{1.0, 0.0}),
                new Document("doc-2", "재무제표", new double[]{0.0, 1.0}),
                new Document("doc-3", "보험청약", new double[]{0.5, 0.5})
        );

        kMeans.initialize(docs);

        assertThat(docs)
                .extracting(Document::getId)
                .containsExactly("doc-1", "doc-2", "doc-3");
    }

    @Test
    void assignGroupsDocumentsByNearestCentroid() {
        KMeans kMeans = new KMeans(2, new Random(1));
        Centroid hr = new Centroid(new double[]{1.0, 0.0});
        Centroid finance = new Centroid(new double[]{0.0, 1.0});
        List<Document> docs = List.of(
                new Document("parental-leave", "육아휴직", new double[]{0.9, 0.1}),
                new Document("maternity-leave", "출산휴가", new double[]{0.8, 0.2}),
                new Document("financial-statement", "재무제표", new double[]{0.1, 0.9})
        );

        Map<Centroid, List<Document>> assignments = kMeans.assign(docs, List.of(hr, finance));

        assertThat(assignments.get(hr))
                .extracting(Document::getId)
                .containsExactly("parental-leave", "maternity-leave");
        assertThat(assignments.get(finance))
                .extracting(Document::getId)
                .containsExactly("financial-statement");
    }

    @Test
    void assignKeepsEmptyClusterWhenNoDocumentIsClosest() {
        KMeans kMeans = new KMeans(3, new Random(1));
        Centroid hr = new Centroid(new double[]{1.0, 0.0});
        Centroid finance = new Centroid(new double[]{0.0, 1.0});
        Centroid insurance = new Centroid(new double[]{-1.0, 0.0});
        List<Document> docs = List.of(
                new Document("parental-leave", "육아휴직", new double[]{0.9, 0.1}),
                new Document("financial-statement", "재무제표", new double[]{0.1, 0.9})
        );

        Map<Centroid, List<Document>> assignments = kMeans.assign(docs, List.of(hr, finance, insurance));

        assertThat(assignments.get(insurance)).isEmpty();
    }

    @Test
    void rejectsInvalidInput() {
        assertThatThrownBy(() -> new KMeans(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KMeans(1).initialize(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KMeans(2).initialize(List.of(
                new Document("doc-1", "육아휴직", new double[]{1.0, 0.0})
        ))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KMeans(1).assign(List.of(), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
