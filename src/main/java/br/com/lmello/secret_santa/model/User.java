package br.com.lmello.secret_santa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Table(name = "users")
@Entity(name = "User")
@Getter
@Setter
@AllArgsConstructor
public class User extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String description;

    private String key;

    private int usage;
}
