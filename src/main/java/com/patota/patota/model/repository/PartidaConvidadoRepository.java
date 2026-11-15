package com.patota.patota.model.repository;

import com.patota.patota.model.entity.PartidaConvidadoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaConvidadoRepository extends JpaRepository<PartidaConvidadoEntity, Long> {
    List<PartidaConvidadoEntity> findAllByPartida_IdOrderById(Long partidaId);
}
