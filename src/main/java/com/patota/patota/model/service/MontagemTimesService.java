package com.patota.patota.model.service;

import com.patota.patota.model.dto.AtualizarEstatisticasDTO;
import com.patota.patota.model.dto.CriarTimePartidaDTO;
import com.patota.patota.model.dto.ParticipanteTimeRespostaDTO;
import com.patota.patota.model.dto.TimePartidaRespostaDTO;
import com.patota.patota.model.entity.JogadorPartidaEntity;
import com.patota.patota.model.entity.ParticipanteDaPartida;
import com.patota.patota.model.entity.PartidaConvidadoEntity;
import com.patota.patota.model.entity.PartidaEntity;
import com.patota.patota.model.entity.TimePartidaEntity;
import com.patota.patota.model.enums.EStatusPartida;
import com.patota.patota.model.enums.EStatusParticipante;
import com.patota.patota.model.enums.ETipoParticipantePartida;
import com.patota.patota.model.enums.ETipoJogador;
import com.patota.patota.model.repository.JogadorPartidaRepository;
import com.patota.patota.model.repository.PartidaConvidadoRepository;
import com.patota.patota.model.repository.TimePartidaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MontagemTimesService {

    private final PartidaService partidaService;
    private final TimePartidaRepository times;
    private final JogadorPartidaRepository mensalistas;
    private final PartidaConvidadoRepository convidados;

    @Transactional
    public TimePartidaRespostaDTO criar(Long partidaId, CriarTimePartidaDTO dados) {
        PartidaEntity partida = exigirPartidaAgendada(partidaId);
        if (dados == null || dados.nome() == null || dados.nome().isBlank()
                || dados.nome().length() > 50) {
            throw new IllegalArgumentException("O nome do time é obrigatório e deve ter até 50 caracteres.");
        }
        String nome = dados.nome().trim();
        if (times.existsByPartida_IdAndNomeIgnoreCase(partidaId, nome)) {
            throw new IllegalStateException("Já existe um time com esse nome na partida.");
        }
        TimePartidaEntity time = new TimePartidaEntity();
        time.setPartida(partida);
        time.setNome(nome);
        time.setCor(dados.cor() == null || dados.cor().isBlank() ? null : dados.cor().trim());
        return TimePartidaRespostaDTO.de(times.save(time), List.of());
    }

    @Transactional(readOnly = true)
    public List<TimePartidaRespostaDTO> listar(Long partidaId) {
        partidaService.buscar(partidaId);
        return times.findAllByPartida_IdOrderById(partidaId).stream()
                .map(time -> TimePartidaRespostaDTO.de(time, participantesDoTime(time.getId())))
                .toList();
    }

    @Transactional
    public List<TimePartidaRespostaDTO> balancear(Long partidaId) {
        PartidaEntity partida = exigirPartidaAgendada(partidaId);
        List<TimePartidaEntity> timesDaPartida = times.findAllByPartida_IdOrderById(partidaId);
        if (timesDaPartida.size() < 2) {
            throw new IllegalStateException("Crie pelo menos dois times antes de balancear.");
        }
        List<ParticipanteDaPartida> todos = new ArrayList<>();
        todos.addAll(mensalistas.findAllByPartida_IdOrderById(partidaId));
        todos.addAll(convidados.findAllByPartida_IdOrderById(partidaId));
        if (todos.stream().anyMatch(p -> p.getGols() != 0 || p.getAssistencias() != 0)) {
            throw new IllegalStateException("Não é possível rebalancear após registrar estatísticas.");
        }
        List<ParticipanteDaPartida> confirmados = todos.stream()
                .filter(p -> p.getStatus() == EStatusParticipante.CONFIRMADO).toList();
        if (confirmados.size() > partida.getMaximoJogadores()) {
            throw new IllegalStateException("Há mais confirmados que o máximo da partida.");
        }
        List<ParticipanteDaPartida> goleiros = confirmados.stream()
                .filter(p -> p.getTipo() == ETipoJogador.GOLEIRO)
                .sorted(Comparator.comparingInt(ParticipanteDaPartida::getNota).reversed()
                        .thenComparing(ParticipanteDaPartida::getId))
                .toList();
        if (goleiros.size() != timesDaPartida.size()) {
            throw new IllegalStateException("É necessário um goleiro confirmado para cada time.");
        }
        List<ParticipanteDaPartida> jogadoresDeLinha = confirmados.stream()
                .filter(p -> p.getTipo() == ETipoJogador.LINHA)
                .sorted(Comparator.comparingInt(ParticipanteDaPartida::getNota).reversed()
                        .thenComparing(ParticipanteDaPartida::getId))
                .toList();

        int[] quantidades = new int[timesDaPartida.size()];
        int[] somas = new int[timesDaPartida.size()];
        for (int i = 0; i < goleiros.size(); i++) {
            atribuirNoBalanceamento(goleiros.get(i), timesDaPartida.get(i), i, quantidades, somas);
        }
        for (ParticipanteDaPartida jogador : jogadoresDeLinha) {
            int escolhido = 0;
            for (int i = 1; i < timesDaPartida.size(); i++) {
                if (quantidades[i] < quantidades[escolhido]
                        || (quantidades[i] == quantidades[escolhido] && somas[i] < somas[escolhido])) {
                    escolhido = i;
                }
            }
            atribuirNoBalanceamento(jogador, timesDaPartida.get(escolhido), escolhido, quantidades, somas);
        }
        mensalistas.flush();
        convidados.flush();
        return listar(partidaId);
    }

    @Transactional
    public ParticipanteTimeRespostaDTO atribuir(
            Long partidaId, ETipoParticipantePartida tipo, Long participanteId, Long timeId) {
        exigirPartidaAgendada(partidaId);
        ParticipanteDaPartida participante = buscarParticipante(partidaId, tipo, participanteId);
        exigirConfirmado(participante);
        TimePartidaEntity time = times.findById(timeId)
                .filter(encontrado -> encontrado.getPartida().getId().equals(partidaId))
                .orElseThrow(() -> new EntityNotFoundException("Time não encontrado nesta partida."));
        participante.setTimePartida(time);
        salvar(participante);
        return resposta(participante, tipo);
    }

    @Transactional
    public ParticipanteTimeRespostaDTO removerDoTime(
            Long partidaId, ETipoParticipantePartida tipo, Long participanteId) {
        exigirPartidaAgendada(partidaId);
        ParticipanteDaPartida participante = buscarParticipante(partidaId, tipo, participanteId);
        participante.setTimePartida(null);
        salvar(participante);
        return resposta(participante, tipo);
    }

    @Transactional
    public ParticipanteTimeRespostaDTO atualizarEstatisticas(
            Long partidaId, ETipoParticipantePartida tipo, Long participanteId,
            AtualizarEstatisticasDTO dados) {
        exigirPartidaAgendada(partidaId);
        if (dados == null || dados.gols() == null || dados.assistencias() == null
                || dados.gols() < 0 || dados.assistencias() < 0) {
            throw new IllegalArgumentException("Gols e assistências não podem ser negativos.");
        }
        ParticipanteDaPartida participante = buscarParticipante(partidaId, tipo, participanteId);
        exigirConfirmado(participante);
        if (participante.getTimePartida() == null) {
            throw new IllegalStateException("O participante precisa estar em um time.");
        }
        participante.setGols(dados.gols());
        participante.setAssistencias(dados.assistencias());
        salvar(participante);
        return resposta(participante, tipo);
    }

    private PartidaEntity exigirPartidaAgendada(Long partidaId) {
        PartidaEntity partida = partidaService.buscar(partidaId);
        if (partida.getStatus() != EStatusPartida.AGENDADA) {
            throw new IllegalStateException("A partida precisa estar agendada para montar os times.");
        }
        return partida;
    }

    private void exigirConfirmado(ParticipanteDaPartida participante) {
        if (participante.getStatus() != EStatusParticipante.CONFIRMADO) {
            throw new IllegalStateException("O participante precisa estar confirmado.");
        }
    }

    private ParticipanteDaPartida buscarParticipante(
            Long partidaId, ETipoParticipantePartida tipo, Long participanteId) {
        if (tipo == ETipoParticipantePartida.MENSALISTA) {
            return mensalistas.findById(participanteId)
                    .filter(p -> p.getPartida().getId().equals(partidaId))
                    .orElseThrow(() -> new EntityNotFoundException("Mensalista não encontrado nesta partida."));
        }
        return convidados.findById(participanteId)
                .filter(p -> p.getPartida().getId().equals(partidaId))
                .orElseThrow(() -> new EntityNotFoundException("Convidado não encontrado nesta partida."));
    }

    private void salvar(ParticipanteDaPartida participante) {
        if (participante instanceof JogadorPartidaEntity mensalista) {
            mensalistas.save(mensalista);
        } else if (participante instanceof PartidaConvidadoEntity convidado) {
            convidados.save(convidado);
        }
    }

    private void atribuirNoBalanceamento(
            ParticipanteDaPartida participante, TimePartidaEntity time,
            int indice, int[] quantidades, int[] somas) {
        participante.setTimePartida(time);
        salvar(participante);
        quantidades[indice]++;
        somas[indice] += participante.getNota();
    }

    private ParticipanteTimeRespostaDTO resposta(
            ParticipanteDaPartida participante, ETipoParticipantePartida tipo) {
        String nome = participante instanceof JogadorPartidaEntity mensalista
                ? mensalista.getJogador().getNome()
                : ((PartidaConvidadoEntity) participante).getNome();
        return ParticipanteTimeRespostaDTO.de(participante, tipo, nome);
    }

    private List<ParticipanteTimeRespostaDTO> participantesDoTime(Long timeId) {
        List<ParticipanteTimeRespostaDTO> participantes = new ArrayList<>();
        mensalistas.findAllByTimePartida_IdOrderById(timeId).forEach(mensalista -> participantes.add(
                resposta(mensalista, ETipoParticipantePartida.MENSALISTA)));
        convidados.findAllByTimePartida_IdOrderById(timeId).forEach(convidado -> participantes.add(
                resposta(convidado, ETipoParticipantePartida.CONVIDADO)));
        return participantes;
    }
}
