package br.com.lmello.secret_santa.repository;

import br.com.lmello.secret_santa.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, String> {
    @Query("SELECT p FROM Participant p WHERE lower(p.name) = lower(:name) and lower(p.email) = lower(:email) ")
    Optional<Participant> getParticipantByNameAndEmailC(String name, String email);
}
