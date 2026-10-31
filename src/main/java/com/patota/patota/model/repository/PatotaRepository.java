package com.patota.patota.model.repository;

import com.patota.patota.model.entity.PatotaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatotaRepository extends JpaRepository<PatotaEntity, Long> {
}
