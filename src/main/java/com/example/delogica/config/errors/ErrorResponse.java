package com.example.delogica.config.errors;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String timestamp;
    private String path;
    private int status;
    private String error;
    private ErrorCode code;
    private String message;
    private List<ErrorDetail> details;
    // private String traceId; 
}
