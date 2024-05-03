package br.com.lmello.secret_santa.repository;

import br.com.lmello.secret_santa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByKey(String key);
}
