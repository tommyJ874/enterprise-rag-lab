package com.example.enterpriseraglab;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimilarityTests {

    @Test
    void cosineReturnsOneForSameDirection() {
        double score = Similarity.cosine(
                new double[]{1.0, 1.0},
                new double[]{2.0, 2.0}
        );

        assertThat(score).isCloseTo(1.0, within(0.000000001));
    }

    @Test
    void cosineReturnsZeroForOrthogonalVectors() {
        double score = Similarity.cosine(
                new double[]{1.0, 0.0},
                new double[]{0.0, 1.0}
        );

        assertThat(score).isEqualTo(0.0);
    }

    @Test
    void cosineRejectsDifferentDimensions() {
        assertThatThrownBy(() -> Similarity.cosine(
                new double[]{1.0, 2.0},
                new double[]{1.0}
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
