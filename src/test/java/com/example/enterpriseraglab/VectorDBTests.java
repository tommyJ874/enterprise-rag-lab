package com.example.enterpriseraglab;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorDBTests {

    @Test
    void insertStoresDocuments() {
        VectorDB vectorDB = new VectorDB();

        vectorDB.insert(new Document("doc-1", "육아휴직", new double[]{1.0, 0.0}));

        assertThat(vectorDB.size()).isEqualTo(1);
    }

    @Test
    void searchReturnsDocumentsSortedByCosineSimilarity() {
        VectorDB vectorDB = new VectorDB();
        vectorDB.insert(new Document("finance", "재무제표", new double[]{0.1, 0.9}));
        vectorDB.insert(new Document("leave", "경조휴가", new double[]{0.8, 0.2}));
        vectorDB.insert(new Document("parental-leave", "육아휴직", new double[]{1.0, 0.0}));
        vectorDB.insert(new Document("insurance", "보험청약", new double[]{0.4, 0.6}));

        List<SearchResult> results = vectorDB.search(new double[]{1.0, 0.0});

        assertThat(results)
                .extracting(result -> result.getDocument().getId())
                .containsExactly("parental-leave", "leave", "insurance", "finance");
    }

    @Test
    void searchReturnsTopKResults() {
        VectorDB vectorDB = new VectorDB();
        vectorDB.insert(new Document("finance", "재무제표", new double[]{0.1, 0.9}));
        vectorDB.insert(new Document("leave", "경조휴가", new double[]{0.8, 0.2}));
        vectorDB.insert(new Document("parental-leave", "육아휴직", new double[]{1.0, 0.0}));
        vectorDB.insert(new Document("insurance", "보험청약", new double[]{0.4, 0.6}));

        List<SearchResult> results = vectorDB.search(new double[]{1.0, 0.0}, 3);

        assertThat(results)
                .hasSize(3)
                .extracting(result -> result.getDocument().getId())
                .containsExactly("parental-leave", "leave", "insurance");
    }

    @Test
    void searchRejectsNegativeTopK() {
        VectorDB vectorDB = new VectorDB();

        assertThatThrownBy(() -> vectorDB.search(new double[]{1.0, 0.0}, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
