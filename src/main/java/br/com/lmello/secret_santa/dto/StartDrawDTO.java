package br.com.lmello.secret_santa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record StartDrawDTO(
        @JsonProperty
        @NotNull
        String adminCode,
        String message
) {
}
