package com.example.delogica.config.errors;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErrorDetail {
    private String field;
    private String message;
}