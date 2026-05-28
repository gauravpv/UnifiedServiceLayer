package com.bajaj.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"request", "reference_id", "source_system"})
public class EncryptedRequestEnvelope {

    @NotBlank(message = "request is required")
    private String request;

    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("source_system")
    private String sourceSystem;
}
