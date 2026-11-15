package com.patota.patota.model.repository;

import com.patota.patota.model.entity.PartidaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaRepository extends JpaRepository<PartidaEntity, Long> {
    List<PartidaEntity> findAllByPatota_IdOrderByDataHoraDesc(Long patotaId);
}
