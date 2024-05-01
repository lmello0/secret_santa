package br.com.lmello.secret_santa.dto;

import br.com.lmello.secret_santa.model.Draw;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DrawDTO(
        String code,
        String adminCode,
        boolean started,
        @JsonProperty
        @NotNull
        int budget,
        @JsonProperty
        @NotNull
        List<ParticipantDTO> participants
) {
    public DrawDTO(Draw draw) {
        this(
                draw.getCode(),
                draw.getAdminCode(),
                draw.isStarted(),
                draw.getBudget(),
                draw.getParticipants()
                        .stream()
                        .map(p -> new ParticipantDTO(p.getName(), p.getEmail()))
                        .toList()
        );
    }
}
