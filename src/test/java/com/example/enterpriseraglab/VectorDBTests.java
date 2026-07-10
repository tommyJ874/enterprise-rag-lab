package com.example.enterpriseraglab;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

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
        vectorDB.insert(new Document("finance", "재무제표", new double[]{0.0, 1.0}, 2));
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
    void centroidReturnsAverageVectorForCluster() {
        VectorDB vectorDB = new VectorDB();
        vectorDB.insert(new Document("parental-leave", "육아휴직", new double[]{1.0, 0.0}, 1));
        vectorDB.insert(new Document("maternity-leave", "출산휴가", new double[]{0.8, 0.2}, 1));

        double[] centroid = vectorDB.centroid(1);

        assertThat(centroid[0]).isCloseTo(0.9, within(0.000000001));
        assertThat(centroid[1]).isCloseTo(0.1, within(0.000000001));
    }

    @Test
    void nearestClusterReturnsClusterWithClosestCentroid() {
        VectorDB vectorDB = new VectorDB();
        vectorDB.insert(new Document("parental-leave", "육아휴직", new double[]{1.0, 0.0}, 1));
        vectorDB.insert(new Document("maternity-leave", "출산휴가", new double[]{0.8, 0.2}, 1));
        vectorDB.insert(new Document("finance", "재무제표", new double[]{0.0, 1.0}, 2));
        vectorDB.insert(new Document("settlement", "결산", new double[]{0.2, 0.8}, 2));

        int nearestCluster = vectorDB.nearestCluster(new double[]{1.0, 0.0});

        assertThat(nearestCluster).isEqualTo(1);
    }

    @Test
    void searchNearestClusterSearchesOnlyClosestCluster() {
        VectorDB vectorDB = new VectorDB();
        vectorDB.insert(new Document("finance", "재무제표", new double[]{0.0, 1.0}, 2));
        vectorDB.insert(new Document("leave", "경조휴가", new double[]{0.8, 0.2}, 1));
        vectorDB.insert(new Document("parental-leave", "육아휴직", new double[]{1.0, 0.0}, 1));
        vectorDB.insert(new Document("insurance", "보험청약", new double[]{0.0, 1.0}, 3));

        List<SearchResult> results = vectorDB.searchNearestCluster(new double[]{1.0, 0.0}, 2);

        assertThat(results)
                .hasSize(2)
                .extracting(result -> result.getDocument().getId())
                .containsExactly("parental-leave", "leave");
    }

    @Test
    void searchNearestClusterReturnsEmptyListWhenDatabaseIsEmpty() {
        VectorDB vectorDB = new VectorDB();

        List<SearchResult> results = vectorDB.searchNearestCluster(new double[]{1.0, 0.0});

        assertThat(results).isEmpty();
    }

    @Test
    void centroidRejectsUnknownCluster() {
        VectorDB vectorDB = new VectorDB();

        assertThatThrownBy(() -> vectorDB.centroid(1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nearestClusterRejectsEmptyDatabase() {
        VectorDB vectorDB = new VectorDB();

        assertThatThrownBy(() -> vectorDB.nearestCluster(new double[]{1.0, 0.0}))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void searchRejectsNegativeTopK() {
        VectorDB vectorDB = new VectorDB();

        assertThatThrownBy(() -> vectorDB.search(new double[]{1.0, 0.0}, 1, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vectorDB.searchNearestCluster(new double[]{1.0, 0.0}, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vectorDB.bruteForceSearch(new double[]{1.0, 0.0}, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
