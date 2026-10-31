package com.patota.patota.model.service;

import com.patota.patota.model.entity.PartidaEntity;
import com.patota.patota.model.entity.PatotaEntity;
import com.patota.patota.model.entity.PatotaJogadorEntity;
import com.patota.patota.model.dto.CriarPartidaDTO;
import com.patota.patota.model.enums.EStatusPartida;
import com.patota.patota.model.repository.PartidaRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final PatotaService patotaService;
    private final PatotaJogadorService patotaJogadorService;
    private final PresencaPartidaService presencaPartidaService;

    /**
     * Cria uma partida agendada para uma patota ativa. Times e escalações são
     * definidos posteriormente, depois da confirmação dos participantes.
     */
    @Transactional
    public PartidaEntity criar(CriarPartidaDTO dto) {
        validarDadosCriacao(dto);
        PatotaEntity patota = buscarPatotaValida(dto.patotaId());

        PartidaEntity partida = new PartidaEntity();
        partida.setPatota(patota);
        partida.setDataHora(dto.dataHora());
        partida.setLocal(patota.getLocal());
        partida.setObservacoes(dto.observacoes());
        partida.setStatus(EStatusPartida.AGENDADA);

        PartidaEntity partidaCriada = partidaRepository.save(partida);
        criarConvitesPendentes(partidaCriada);

        return partidaCriada;
    }

    private void validarDadosCriacao(CriarPartidaDTO dto) {
        Objects.requireNonNull(dto, "Os dados da partida são obrigatórios.");

        if (dto.patotaId() == null) {
            throw new IllegalArgumentException("A patota da partida é obrigatória.");
        }
        if (dto.dataHora() == null) {
            throw new IllegalArgumentException("A data e hora da partida são obrigatórias.");
        }
    }

    private PatotaEntity buscarPatotaValida(Long patotaId) {
        PatotaEntity patota = patotaService.buscarPatota(patotaId);
        if (!patota.isAtiva()) {
            throw new IllegalStateException("Não é possível criar uma partida para uma patota inativa.");
        }
        if (patota.getLocal() == null || patota.getLocal().isBlank()) {
            throw new IllegalStateException("A patota precisa ter um local cadastrado para criar uma partida.");
        }
        return patota;
    }

    private void criarConvitesPendentes(PartidaEntity partida) {
        List<PatotaJogadorEntity> mensalistas = patotaJogadorService
                .buscarMensalistasAtivosDaPatota(partida.getPatota().getId());

        presencaPartidaService.criarConvitesPendentes(partida, mensalistas);
    }
}
