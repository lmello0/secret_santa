package br.com.lmello.secret_santa.dto;

import br.com.lmello.secret_santa.model.Participant;

public record DrawEmailDTO(
        Participant sender,
        Participant receiver,
        String drawCode,
        int budget
) {
}
