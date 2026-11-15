package com.patota.patota.model.service;

import com.patota.patota.model.entity.PartidaEntity;
import com.patota.patota.model.entity.PatotaJogadorEntity;
import com.patota.patota.model.entity.JogadorPartidaEntity;
import com.patota.patota.model.enums.EStatusParticipante;
import com.patota.patota.model.repository.JogadorPartidaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JogadorPartidaService {

    private final JogadorPartidaRepository jogadorPartidaRepository;

    /** Cria registros pendentes para os mensalistas ativos da patota. */
    public void criarRegistrosPendentes(PartidaEntity partida, List<PatotaJogadorEntity> mensalistas) {
        List<JogadorPartidaEntity> jogadoresDaPartida = mensalistas.stream()
                .map(mensalista -> criarRegistroPendente(partida, mensalista))
                .toList();

        jogadorPartidaRepository.saveAll(jogadoresDaPartida);
    }

    private JogadorPartidaEntity criarRegistroPendente(
            PartidaEntity partida, PatotaJogadorEntity mensalista) {
        JogadorPartidaEntity jogadorPartida = new JogadorPartidaEntity();
        jogadorPartida.setPartida(partida);
        jogadorPartida.setJogador(mensalista.getJogador());
        jogadorPartida.setStatus(EStatusParticipante.PENDENTE);
        return jogadorPartida;
    }
}
