package br.com.lmello.secret_santa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "users")
@Entity(name = "User")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String description;

    private String key;

    private int usage = 0;

    public void increaseUsage() {
        this.lastModifiedAt = LocalDateTime.now();
        this.usage++;
    }
}
