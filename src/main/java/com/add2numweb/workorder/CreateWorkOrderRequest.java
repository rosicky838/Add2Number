package com.add2numweb.workorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = false)
public record CreateWorkOrderRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 50, message = "must not exceed 50 characters")
        String equipmentId,
        @NotBlank(message = "must not be blank")
        @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "must be one of: LOW, MEDIUM, HIGH")
        String priority) {
}
