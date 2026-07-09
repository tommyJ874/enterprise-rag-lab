package com.example.enterpriseraglab;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorDBTests {

    @Test
    void insertStoresDocuments() {
        VectorDB vectorDB = new VectorDB();

        vectorDB.insert(new Document("doc-1", "육아휴직", new double[]{1.0, 0.0}, 1));

        assertThat(vectorDB.size()).isEqualTo(1);
        assertThat(vectorDB.clusterSize(1)).isEqualTo(1);
    }

    @Test
    void bruteForceSearchReturnsDocumentsSortedByCosineSimilarity() {
        VectorDB vectorDB = new VectorDB();
        vectorDB.insert(new Document("finance", "재무제표", new double[]{0.1, 0.9}, 2));
        vectorDB.insert(new Document("leave", "경조휴가", new double[]{0.8, 0.2}, 1));
        vectorDB.insert(new Document("parental-leave", "육아휴직", new double[]{1.0, 0.0}, 1));
        vectorDB.insert(new Document("insurance", "보험청약", new double[]{0.4, 0.6}, 3));

        List<SearchResult> results = vectorDB.bruteForceSearch(new double[]{1.0, 0.0});

        assertThat(results)
                .extracting(result -> result.getDocument().getId())
                .containsExactly("parental-leave", "leave", "insurance", "finance");
    }

    @Test
    void clusterSearchScansOnlyDocumentsInTheSelectedCluster() {
        VectorDB vectorDB = new VectorDB();
        vectorDB.insert(new Document("finance", "재무제표", new double[]{1.0, 0.0}, 2));
        vectorDB.insert(new Document("leave", "경조휴가", new double[]{0.8, 0.2}, 1));
        vectorDB.insert(new Document("parental-leave", "육아휴직", new double[]{1.0, 0.0}, 1));
        vectorDB.insert(new Document("insurance", "보험청약", new double[]{0.4, 0.6}, 3));

        List<SearchResult> results = vectorDB.search(new double[]{1.0, 0.0}, 1);

        assertThat(results)
                .extracting(result -> result.getDocument().getId())
                .containsExactly("parental-leave", "leave");
    }

    @Test
    void clusterSearchReturnsTopKResults() {
        VectorDB vectorDB = new VectorDB();
        vectorDB.insert(new Document("annual-leave", "연차", new double[]{0.7, 0.3}, 1));
        vectorDB.insert(new Document("leave", "경조휴가", new double[]{0.8, 0.2}, 1));
        vectorDB.insert(new Document("parental-leave", "육아휴직", new double[]{1.0, 0.0}, 1));
        vectorDB.insert(new Document("maternity-leave", "출산휴가", new double[]{0.9, 0.1}, 1));

        List<SearchResult> results = vectorDB.search(new double[]{1.0, 0.0}, 1, 3);

        assertThat(results)
                .hasSize(3)
                .extracting(result -> result.getDocument().getId())
                .containsExactly("parental-leave", "maternity-leave", "leave");
    }

    @Test
    void clusterSearchReturnsEmptyListWhenClusterDoesNotExist() {
        VectorDB vectorDB = new VectorDB();
        vectorDB.insert(new Document("parental-leave", "육아휴직", new double[]{1.0, 0.0}, 1));

        List<SearchResult> results = vectorDB.search(new double[]{1.0, 0.0}, 99);

        assertThat(results).isEmpty();
    }

    @Test
    void searchRejectsNegativeTopK() {
        VectorDB vectorDB = new VectorDB();

        assertThatThrownBy(() -> vectorDB.search(new double[]{1.0, 0.0}, 1, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vectorDB.bruteForceSearch(new double[]{1.0, 0.0}, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
