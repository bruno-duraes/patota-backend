package com.patota.patota.model.repository;

import com.patota.patota.model.entity.PatotaJogadorEntity;
import com.patota.patota.model.enums.ECargoPatota;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatotaJogadorRepository extends JpaRepository<PatotaJogadorEntity, Long> {

    List<PatotaJogadorEntity> findAllByPatota_IdAndAtivoTrueAndJogador_AtivoTrue(Long patotaId);

    Optional<PatotaJogadorEntity> findByPatota_IdAndJogador_Id(Long patotaId, Long jogadorId);

    boolean existsByPatota_IdAndJogador_IdAndAtivoTrue(Long patotaId, Long jogadorId);

    boolean existsByPatota_IdAndJogador_IdAndAtivoTrueAndCargo(
            Long patotaId, Long jogadorId, ECargoPatota cargo);

    long countByPatota_IdAndAtivoTrueAndCargo(Long patotaId, ECargoPatota cargo);
}
