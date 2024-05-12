package br.com.lmello.secret_santa.repository;

import br.com.lmello.secret_santa.model.Draw;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DrawRepository extends JpaRepository<Draw, String> {
    Optional<Draw> findByCode(String code);
}
