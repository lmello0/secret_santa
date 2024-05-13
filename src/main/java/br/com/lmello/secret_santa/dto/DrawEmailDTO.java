package br.com.lmello.secret_santa.dto;

import br.com.lmello.secret_santa.model.Participant;

import java.math.BigDecimal;

public record DrawEmailDTO(
        Participant sender,
        Participant receiver,
        String drawCode,
        BigDecimal budget
) {
}
