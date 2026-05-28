package com.bajaj.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"response", "status_code", "message", "bre_tat"})
public class EncryptedResponseEnvelope {

    private String response;

    @JsonProperty("status_code")
    private String statusCode;

    private String message;

    @JsonProperty("bre_tat")
    private String breTat;
}
