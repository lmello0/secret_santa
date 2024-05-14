package br.com.lmello.secret_santa.model;

import br.com.lmello.secret_santa.dto.ParticipantDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "participants")
@Entity(name = "Participant")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Participant extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String email;

    @Transient
    private Participant from;

    @Transient
    private Participant to;

    @Transient
    private boolean selected = false;

    public Participant(ParticipantDTO participantDTO) {
        this.name = participantDTO.name();
        this.email = participantDTO.email();
    }
}
