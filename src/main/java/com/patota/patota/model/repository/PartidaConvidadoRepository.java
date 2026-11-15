package com.patota.patota.model.repository;

import com.patota.patota.model.entity.PartidaConvidadoEntity;
import com.patota.patota.model.enums.EStatusParticipante;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaConvidadoRepository extends JpaRepository<PartidaConvidadoEntity, Long> {
    List<PartidaConvidadoEntity> findAllByPartida_IdOrderById(Long partidaId);

    List<PartidaConvidadoEntity> findAllByTimePartida_IdOrderById(Long timeId);

    long countByPartida_IdAndStatus(Long partidaId, EStatusParticipante status);
}
