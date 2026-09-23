package com.add2numweb.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WorkOrderIdGenerationExceptionTest {

    @Test
    void preservesCauseWhenProvided() {
        IllegalStateException cause = new IllegalStateException("collision");

        WorkOrderIdGenerationException exception =
                new WorkOrderIdGenerationException("generation failed", cause);

        assertThat(exception)
                .hasMessage("generation failed")
                .hasCause(cause);
    }
}
