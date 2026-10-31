package com.patota.patota.model.repository;

import com.patota.patota.model.entity.PatotaJogadorEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatotaJogadorRepository extends JpaRepository<PatotaJogadorEntity, Long> {

    List<PatotaJogadorEntity> findAllByPatota_IdAndAtivoTrueAndJogador_AtivoTrue(Long patotaId);

    Optional<PatotaJogadorEntity> findByPatota_IdAndJogador_Id(Long patotaId, Long jogadorId);
}
