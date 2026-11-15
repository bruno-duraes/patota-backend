package com.patota.patota.model.service;

import com.patota.patota.model.dto.ConvidarJogadorDTO;
import com.patota.patota.model.dto.ParticipantesPartidaRespostaDTO;
import com.patota.patota.model.dto.PartidaConvidadoRespostaDTO;
import com.patota.patota.model.dto.JogadorPartidaRespostaDTO;
import com.patota.patota.model.entity.JogadorEntity;
import com.patota.patota.model.entity.JogadorPartidaEntity;
import com.patota.patota.model.entity.PartidaEntity;
import com.patota.patota.model.entity.PartidaConvidadoEntity;
import com.patota.patota.model.enums.EStatusPartida;
import com.patota.patota.model.enums.EStatusParticipante;
import com.patota.patota.model.enums.ETipoJogador;
import com.patota.patota.model.repository.PatotaJogadorRepository;
import com.patota.patota.model.repository.PartidaConvidadoRepository;
import com.patota.patota.model.repository.JogadorPartidaRepository;
import com.patota.patota.model.repository.PartidaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

@Service
@RequiredArgsConstructor
public class ParticipantePartidaService {

    private final PartidaService partidaService;
    private final JogadorService jogadorService;
    private final PatotaJogadorService patotaJogadorService;
    private final PatotaJogadorRepository patotaJogadorRepository;
    private final JogadorPartidaRepository jogadorPartidaRepository;
    private final PartidaConvidadoRepository convidadoRepository;
    private final PartidaRepository partidaRepository;

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
        if (dados.nota() != null && (dados.nota() < 1 || dados.nota() > 5)) {
            throw new IllegalArgumentException("A nota deve estar entre 1 e 5.");
        }
        convidado.setNota(dados.nota() == null ? 3 : dados.nota());
        convidado.setTipo(dados.tipo() == null ? ETipoJogador.LINHA : dados.tipo());
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

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public JogadorPartidaRespostaDTO responderMensalista(
            Long partidaId, Long participanteId, Long jogadorId, EStatusParticipante resposta) {
        PartidaEntity partida = bloquearPartida(partidaId);
        JogadorPartidaEntity participante = jogadorPartidaRepository.findById(participanteId)
                .filter(encontrado -> encontrado.getPartida().getId().equals(partidaId))
                .orElseThrow(() -> new EntityNotFoundException("Mensalista não encontrado nesta partida."));
        if (!participante.getJogador().getId().equals(jogadorId)) {
            throw new AccessDeniedException("Somente o próprio jogador pode responder à convocação.");
        }
        if (participante.getPartida().getStatus() != EStatusPartida.AGENDADA
                || participante.getStatus() != EStatusParticipante.PENDENTE) {
            throw new IllegalStateException("Só é possível responder a uma convocação pendente de partida agendada.");
        }
        if (resposta != EStatusParticipante.CONFIRMADO && resposta != EStatusParticipante.RECUSADO) {
            throw new IllegalArgumentException("Resposta inválida para o mensalista.");
        }
        if (resposta == EStatusParticipante.CONFIRMADO) {
            exigirVaga(partida);
        }
        participante.setStatus(resposta);
        participante.setRespondidaEm(LocalDateTime.now());
        return JogadorPartidaRespostaDTO.de(jogadorPartidaRepository.save(participante));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PartidaConvidadoRespostaDTO aprovar(Long partidaId, Long convidadoId, Long gerenteId) {
        return decidir(partidaId, convidadoId, gerenteId, EStatusParticipante.CONFIRMADO);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PartidaConvidadoRespostaDTO recusar(Long partidaId, Long convidadoId, Long gerenteId) {
        return decidir(partidaId, convidadoId, gerenteId, EStatusParticipante.RECUSADO);
    }

    private PartidaConvidadoRespostaDTO decidir(
            Long partidaId, Long convidadoId, Long gerenteId, EStatusParticipante status) {
        PartidaEntity partida = bloquearPartida(partidaId);
        PartidaConvidadoEntity convidado = buscarConvidadoEntity(partidaId, convidadoId);
        patotaJogadorService.exigirGerente(convidado.getPartida().getPatota().getId(), gerenteId);
        if (convidado.getPartida().getStatus() != EStatusPartida.AGENDADA
                || convidado.getStatus() != EStatusParticipante.PENDENTE) {
            throw new IllegalStateException("Só é possível decidir sobre um convidado pendente de partida agendada.");
        }
        if (status == EStatusParticipante.CONFIRMADO) {
            exigirVaga(partida);
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

    private PartidaEntity bloquearPartida(Long partidaId) {
        return partidaRepository.bloquearPorId(partidaId)
                .orElseThrow(() -> new EntityNotFoundException("Partida não encontrada."));
    }

    private void exigirVaga(PartidaEntity partida) {
        Long partidaId = partida.getId();
        long confirmados = jogadorPartidaRepository
                .countByPartida_IdAndStatus(partidaId, EStatusParticipante.CONFIRMADO)
                + convidadoRepository.countByPartida_IdAndStatus(partidaId, EStatusParticipante.CONFIRMADO);
        if (confirmados >= partida.getMaximoJogadores()) {
            throw new IllegalStateException("A partida atingiu o máximo de jogadores confirmados.");
        }
    }
}
