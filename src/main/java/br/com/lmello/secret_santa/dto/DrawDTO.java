package br.com.lmello.secret_santa.dto;

import br.com.lmello.secret_santa.model.Draw;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DrawDTO(
        String code,
        String adminCode,
        boolean started,
        @JsonProperty
        @NotNull
        BigDecimal budget,
        @JsonProperty
        @NotNull
        List<ParticipantDTO> participants
) {
    public DrawDTO(Draw draw) {
        this(
                draw.getCode(),
                draw.getAdminCode(),
                draw.isStarted(),
                draw.getBudget().setScale(2, RoundingMode.UNNECESSARY),
                draw.getParticipants()
                        .stream()
                        .map(p -> new ParticipantDTO(p.getName(), p.getEmail()))
                        .toList()
        );
    }

    public DrawDTO(BigDecimal budget, List<ParticipantDTO> participants) {
        this(
                null,
                null,
                false,
                budget,
                participants
        );
    }
}
