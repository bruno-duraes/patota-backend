package com.patota.patota.model.repository;

import com.patota.patota.model.entity.PartidaEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartidaRepository extends JpaRepository<PartidaEntity, Long> {
    List<PartidaEntity> findAllByPatota_IdOrderByDataHoraDesc(Long patotaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select partida from PartidaEntity partida where partida.id = :id")
    Optional<PartidaEntity> bloquearPorId(@Param("id") Long id);
}
