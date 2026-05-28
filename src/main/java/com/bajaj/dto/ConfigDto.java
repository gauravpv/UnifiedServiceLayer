package com.bajaj.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigDto {
    private String orgName;
    private String channelName;
    private String productName;
    private String requestId;
    private String caseId;
    private String requestedVersion;
    private String requestTimestamp;
    private String serviceName;
}
