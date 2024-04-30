package br.com.lmello.secret_santa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ParticipantDTO(
        @JsonProperty
        @NotBlank
        String name,

        @JsonProperty
        @NotBlank
        @Email
        String email
) {
}
