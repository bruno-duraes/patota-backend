package com.patota.patota.model.service;

import com.patota.patota.model.entity.PatotaEntity;
import com.patota.patota.model.dto.SalvarPatotaDTO;
import com.patota.patota.model.repository.PatotaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatotaService {

    private final PatotaRepository patotaRepository;

    @Transactional
    public PatotaEntity criar(SalvarPatotaDTO dados) {
        validar(dados);
        PatotaEntity patota = new PatotaEntity();
        preencher(patota, dados);
        return patotaRepository.save(patota);
    }

    @Transactional
    public PatotaEntity atualizar(Long id, SalvarPatotaDTO dados) {
        validar(dados);
        PatotaEntity patota = buscarPatota(id);
        preencher(patota, dados);
        return patotaRepository.save(patota);
    }

    @Transactional
    public PatotaEntity definirAtiva(Long id, boolean ativa) {
        PatotaEntity patota = buscarPatota(id);
        patota.setAtiva(ativa);
        return patotaRepository.save(patota);
    }

    public PatotaEntity buscarPatota(Long id) {
        return patotaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patota não encontrada."));
    }

    private void validar(SalvarPatotaDTO dados) {
        if (dados == null || dados.nome() == null || dados.nome().isBlank()
                || dados.local() == null || dados.local().isBlank()) {
            throw new IllegalArgumentException("Nome e local da patota são obrigatórios.");
        }
    }

    private void preencher(PatotaEntity patota, SalvarPatotaDTO dados) {
        patota.setNome(dados.nome().trim());
        patota.setDescricao(dados.descricao());
        patota.setLocal(dados.local().trim());
    }
}
