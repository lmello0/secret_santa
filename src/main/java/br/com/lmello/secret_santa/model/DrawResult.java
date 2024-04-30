package br.com.lmello.secret_santa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "draw_results")
@Entity(name = "DrawResult")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DrawResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "draw_id", referencedColumnName = "id")
    private Draw draw;

    @ManyToOne
    @JoinColumn(name = "from_id", referencedColumnName = "id")
    private Participant from;

    @ManyToOne
    @JoinColumn(name = "to_id", referencedColumnName = "id")
    private Participant to;
}
