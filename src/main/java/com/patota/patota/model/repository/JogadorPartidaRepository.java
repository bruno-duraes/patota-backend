package com.patota.patota.model.repository;

import com.patota.patota.model.entity.JogadorPartidaEntity;
import com.patota.patota.model.enums.EStatusParticipante;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JogadorPartidaRepository extends JpaRepository<JogadorPartidaEntity, Long> {
    List<JogadorPartidaEntity> findAllByPartida_IdOrderById(Long partidaId);

    List<JogadorPartidaEntity> findAllByTimePartida_IdOrderById(Long timeId);

    long countByPartida_IdAndStatus(Long partidaId, EStatusParticipante status);
}
