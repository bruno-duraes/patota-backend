package com.patota.patota.model.service;

import com.patota.patota.model.entity.PatotaJogadorEntity;
import com.patota.patota.model.entity.PatotaEntity;
import com.patota.patota.model.entity.JogadorEntity;
import com.patota.patota.model.enums.ECargoPatota;
import com.patota.patota.model.repository.PatotaJogadorRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatotaJogadorService {

    private final PatotaJogadorRepository patotaJogadorRepository;
    private final PatotaService patotaService;
    private final JogadorService jogadorService;

    @Transactional
    public PatotaJogadorEntity adicionarMensalista(Long patotaId, Long jogadorId) {
        PatotaEntity patota = patotaService.buscarPatota(patotaId);
        JogadorEntity jogador = jogadorService.buscar(jogadorId);
        if (!patota.isAtiva() || !jogador.isAtivo()) {
            throw new IllegalStateException("Patota e jogador precisam estar ativos.");
        }

        PatotaJogadorEntity vinculo = patotaJogadorRepository
                .findByPatota_IdAndJogador_Id(patotaId, jogadorId)
                .orElseGet(() -> criarVinculo(patota, jogador));
        if (vinculo.isAtivo()) {
            throw new IllegalStateException("Jogador já é mensalista desta patota.");
        }
        vinculo.setAtivo(true);
        vinculo.setSaiuEm(null);
        return patotaJogadorRepository.save(vinculo);
    }

    @Transactional
    public PatotaJogadorEntity removerMensalista(Long patotaId, Long jogadorId) {
        PatotaJogadorEntity vinculo = patotaJogadorRepository
                .findByPatota_IdAndJogador_Id(patotaId, jogadorId)
                .orElseThrow(() -> new EntityNotFoundException("Mensalista não encontrado nesta patota."));
        if (!vinculo.isAtivo()) {
            throw new IllegalStateException("Jogador já não é mensalista desta patota.");
        }
        validarUltimoGerente(vinculo);
        vinculo.setAtivo(false);
        vinculo.setSaiuEm(LocalDateTime.now());
        return patotaJogadorRepository.save(vinculo);
    }

    public List<PatotaJogadorEntity> buscarMensalistasAtivosDaPatota(Long patotaId) {
        patotaService.buscarPatota(patotaId);
        return patotaJogadorRepository.findAllByPatota_IdAndAtivoTrueAndJogador_AtivoTrue(patotaId);
    }

    public void exigirGerente(Long patotaId, Long jogadorId) {
        JogadorEntity jogador = jogadorService.buscar(jogadorId);
        if (!jogador.isAtivo() || !patotaJogadorRepository
                .existsByPatota_IdAndJogador_IdAndAtivoTrueAndCargo(
                        patotaId, jogadorId, ECargoPatota.GERENTE)) {
            throw new AccessDeniedException("A ação exige um gerente ativo desta patota.");
        }
    }

    @Transactional
    public PatotaJogadorEntity definirCargo(
            Long patotaId, Long jogadorId, Long gerenteId, ECargoPatota cargo) {
        exigirGerente(patotaId, gerenteId);
        if (cargo == null) {
            throw new IllegalArgumentException("O cargo é obrigatório.");
        }
        PatotaJogadorEntity vinculo = patotaJogadorRepository
                .findByPatota_IdAndJogador_Id(patotaId, jogadorId)
                .orElseThrow(() -> new EntityNotFoundException("Jogador não pertence à patota."));
        if (!vinculo.isAtivo()) {
            throw new IllegalStateException("O vínculo precisa estar ativo para alterar o cargo.");
        }
        if (vinculo.getCargo() == ECargoPatota.GERENTE && cargo != ECargoPatota.GERENTE) {
            validarUltimoGerente(vinculo);
        }
        vinculo.setCargo(cargo);
        return patotaJogadorRepository.save(vinculo);
    }

    private void validarUltimoGerente(PatotaJogadorEntity vinculo) {
        if (vinculo.getCargo() == ECargoPatota.GERENTE && patotaJogadorRepository
                .countByPatota_IdAndAtivoTrueAndCargo(vinculo.getPatota().getId(), ECargoPatota.GERENTE) <= 1) {
            throw new IllegalStateException("A patota precisa manter pelo menos um gerente ativo.");
        }
    }

    private PatotaJogadorEntity criarVinculo(PatotaEntity patota, JogadorEntity jogador) {
        PatotaJogadorEntity vinculo = new PatotaJogadorEntity();
        vinculo.setPatota(patota);
        vinculo.setJogador(jogador);
        vinculo.setAtivo(false);
        return vinculo;
    }
}
