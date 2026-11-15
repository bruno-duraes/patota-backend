package com.patota.patota.model.service;

import com.patota.patota.model.entity.PatotaEntity;
import com.patota.patota.model.entity.PatotaJogadorEntity;
import com.patota.patota.model.entity.JogadorEntity;
import com.patota.patota.model.enums.ECargoPatota;
import com.patota.patota.model.dto.SalvarPatotaDTO;
import com.patota.patota.model.repository.PatotaRepository;
import com.patota.patota.model.repository.PatotaJogadorRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatotaService {

    private final PatotaRepository patotaRepository;
    private final PatotaJogadorRepository patotaJogadorRepository;
    private final JogadorService jogadorService;

    @Transactional
    public PatotaEntity criar(SalvarPatotaDTO dados) {
        validar(dados);
        PatotaEntity patota = new PatotaEntity();
        preencher(patota, dados);
        return patotaRepository.save(patota);
    }

    @Transactional
    public PatotaEntity criarGerenciada(SalvarPatotaDTO dados, Long gerenteId) {
        JogadorEntity gerente = jogadorService.buscar(gerenteId);
        if (!gerente.isAtivo()) {
            throw new IllegalStateException("O gerente precisa estar ativo.");
        }
        PatotaEntity patota = criar(dados);
        PatotaJogadorEntity vinculo = new PatotaJogadorEntity();
        vinculo.setPatota(patota);
        vinculo.setJogador(gerente);
        vinculo.setCargo(ECargoPatota.GERENTE);
        patotaJogadorRepository.save(vinculo);
        return patota;
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

    public List<PatotaEntity> listar() {
        return patotaRepository.findAll();
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
        if (dados.maximoJogadoresPorPartida() != null) {
            if (dados.maximoJogadoresPorPartida() < 2 || dados.maximoJogadoresPorPartida() > 100) {
                throw new IllegalArgumentException("O máximo de jogadores deve estar entre 2 e 100.");
            }
            patota.setMaximoJogadoresPorPartida(dados.maximoJogadoresPorPartida());
        }
    }
}
