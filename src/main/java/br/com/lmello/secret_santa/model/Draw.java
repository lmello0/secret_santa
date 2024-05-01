package br.com.lmello.secret_santa.model;

import br.com.lmello.secret_santa.dto.DrawDTO;
import br.com.lmello.secret_santa.util.Dictionary;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
    private String generateCode() {
        final int SECTIONS = 3;
        final int SECTION_LENGTH = 3;

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < SECTIONS; i++) {
            for (int j = 0; j < SECTION_LENGTH; j++) {
                int randomIndex = (int) (Math.random() * alphabet.length());
                char randomChar = alphabet.charAt(randomIndex);

                sb.append(randomChar);
            }

            if (i < 2) {
                sb.append("-");
            }
        }

        return sb.toString();
    }

    private String generateAdminCode() {
        final int SECTIONS = 3;
        final int WORDS_SIZE = Dictionary.WORDS.size();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < SECTIONS; i++) {
            int randomIndex = (int) (Math.random() * WORDS_SIZE);

            String randomWord = Dictionary.WORDS.get(randomIndex);

            sb.append(randomWord);

            if (i < 2) {
                sb.append("-");
            }
        }

        return sb.toString();
    }
}
