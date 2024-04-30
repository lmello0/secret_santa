package br.com.lmello.secret_santa.model;

import br.com.lmello.secret_santa.dto.DrawDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Table(name = "draws")
@Entity(name = "Draw")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Draw {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String code;

    private String adminCode;

    private int budget;

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "draws_participants",
            joinColumns = {@JoinColumn(name = "draw_id")},
            inverseJoinColumns = {@JoinColumn(name = "participant_id")}
    )
    private List<Participant> participants;

    private boolean started = false;

    public Draw(DrawDTO drawDTO) {
        this.code = UUID.randomUUID().toString();
        this.adminCode = UUID.randomUUID().toString();
        this.budget = drawDTO.budget();
        this.participants = drawDTO.
                participants().
                stream().
                map(Participant::new)
                .collect(Collectors.toList());
    }

    public void startDraw() {
        this.started = true;
    }
}
