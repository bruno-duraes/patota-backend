package com.patota.patota.model.service;

import com.patota.patota.model.entity.PatotaJogadorEntity;
import com.patota.patota.model.entity.PatotaEntity;
import com.patota.patota.model.entity.JogadorEntity;
import com.patota.patota.model.repository.PatotaJogadorRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
        vinculo.setAtivo(false);
        vinculo.setSaiuEm(LocalDateTime.now());
        return patotaJogadorRepository.save(vinculo);
    }

    public List<PatotaJogadorEntity> buscarMensalistasAtivosDaPatota(Long patotaId) {
        return patotaJogadorRepository.findAllByPatota_IdAndAtivoTrueAndJogador_AtivoTrue(patotaId);
    }

    private PatotaJogadorEntity criarVinculo(PatotaEntity patota, JogadorEntity jogador) {
        PatotaJogadorEntity vinculo = new PatotaJogadorEntity();
        vinculo.setPatota(patota);
        vinculo.setJogador(jogador);
        vinculo.setAtivo(false);
        return vinculo;
    }
}
