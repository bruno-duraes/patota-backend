package com.patota.patota.model.service;

import com.patota.patota.model.dto.ConvidarJogadorDTO;
import com.patota.patota.model.dto.ParticipantesPartidaRespostaDTO;
import com.patota.patota.model.dto.PartidaConvidadoRespostaDTO;
import com.patota.patota.model.dto.JogadorPartidaRespostaDTO;
import com.patota.patota.model.entity.JogadorEntity;
import com.patota.patota.model.entity.PartidaEntity;
import com.patota.patota.model.entity.PartidaConvidadoEntity;
import com.patota.patota.model.enums.EStatusPartida;
import com.patota.patota.model.enums.EStatusParticipante;
import com.patota.patota.model.repository.PatotaJogadorRepository;
import com.patota.patota.model.repository.PartidaConvidadoRepository;
import com.patota.patota.model.repository.JogadorPartidaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipantePartidaService {

    private final PartidaService partidaService;
    private final JogadorService jogadorService;
    private final PatotaJogadorService patotaJogadorService;
    private final PatotaJogadorRepository patotaJogadorRepository;
    private final JogadorPartidaRepository jogadorPartidaRepository;
    private final PartidaConvidadoRepository convidadoRepository;

    @Transactional
    public PartidaConvidadoRespostaDTO convidar(
            Long partidaId, ConvidarJogadorDTO dados, Long convidadorId) {
        if (dados == null || dados.nome() == null || dados.nome().isBlank()
                || dados.nome().length() > 120) {
            throw new IllegalArgumentException("Nome do convidado é obrigatório.");
        }

        PartidaEntity partida = partidaService.buscar(partidaId);
        if (partida.getStatus() != EStatusPartida.AGENDADA) {
            throw new IllegalStateException("Só é possível convidar para uma partida agendada.");
        }

        JogadorEntity convidador = jogadorService.buscar(convidadorId);
        boolean mensalistaAtivo = convidador.isAtivo() && patotaJogadorRepository
                .existsByPatota_IdAndJogador_IdAndAtivoTrue(partida.getPatota().getId(), convidador.getId());
        if (!mensalistaAtivo) {
            throw new IllegalStateException("O jogador que convidou precisa ser mensalista ativo da patota.");
        }

        PartidaConvidadoEntity convidado = new PartidaConvidadoEntity();
        convidado.setPartida(partida);
        convidado.setNome(dados.nome().trim());
        convidado.setConvidadoPorJogador(convidador);
        return PartidaConvidadoRespostaDTO.de(convidadoRepository.save(convidado));
    }

    @Transactional(readOnly = true)
    public ParticipantesPartidaRespostaDTO listar(Long partidaId) {
        partidaService.buscar(partidaId);
        var mensalistas = jogadorPartidaRepository.findAllByPartida_IdOrderById(partidaId).stream()
                .map(JogadorPartidaRespostaDTO::de).toList();
        var convidados = convidadoRepository.findAllByPartida_IdOrderById(partidaId).stream()
                .map(PartidaConvidadoRespostaDTO::de).toList();
        return new ParticipantesPartidaRespostaDTO(mensalistas, convidados);
    }

    @Transactional(readOnly = true)
    public PartidaConvidadoRespostaDTO buscarConvidado(Long partidaId, Long convidadoId) {
        return PartidaConvidadoRespostaDTO.de(buscarConvidadoEntity(partidaId, convidadoId));
    }

    @Transactional
    public PartidaConvidadoRespostaDTO aprovar(Long partidaId, Long convidadoId, Long gerenteId) {
        return decidir(partidaId, convidadoId, gerenteId, EStatusParticipante.CONFIRMADO);
    }

    @Transactional
    public PartidaConvidadoRespostaDTO recusar(Long partidaId, Long convidadoId, Long gerenteId) {
        return decidir(partidaId, convidadoId, gerenteId, EStatusParticipante.RECUSADO);
    }

    private PartidaConvidadoRespostaDTO decidir(
            Long partidaId, Long convidadoId, Long gerenteId, EStatusParticipante status) {
        PartidaConvidadoEntity convidado = buscarConvidadoEntity(partidaId, convidadoId);
        patotaJogadorService.exigirGerente(convidado.getPartida().getPatota().getId(), gerenteId);
        if (convidado.getPartida().getStatus() != EStatusPartida.AGENDADA
                || convidado.getStatus() != EStatusParticipante.PENDENTE) {
            throw new IllegalStateException("Só é possível decidir sobre um convidado pendente de partida agendada.");
        }
        convidado.setStatus(status);
        convidado.setRespondidaEm(LocalDateTime.now());
        return PartidaConvidadoRespostaDTO.de(convidadoRepository.save(convidado));
    }

    private PartidaConvidadoEntity buscarConvidadoEntity(Long partidaId, Long convidadoId) {
        return convidadoRepository.findById(convidadoId)
                .filter(convidado -> convidado.getPartida().getId().equals(partidaId))
                .orElseThrow(() -> new EntityNotFoundException("Convidado não encontrado nesta partida."));
    }
}
