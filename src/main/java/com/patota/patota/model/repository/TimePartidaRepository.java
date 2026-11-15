package com.patota.patota.model.repository;

import com.patota.patota.model.entity.TimePartidaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimePartidaRepository extends JpaRepository<TimePartidaEntity, Long> {
    List<TimePartidaEntity> findAllByPartida_IdOrderById(Long partidaId);

    boolean existsByPartida_IdAndNomeIgnoreCase(Long partidaId, String nome);
}
