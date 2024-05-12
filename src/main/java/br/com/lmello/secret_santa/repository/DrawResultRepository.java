package br.com.lmello.secret_santa.repository;

import br.com.lmello.secret_santa.model.DrawResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrawResultRepository extends JpaRepository<DrawResult, String> {
}
