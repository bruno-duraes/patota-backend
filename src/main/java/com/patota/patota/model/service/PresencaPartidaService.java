package com.patota.patota.model.service;

import com.patota.patota.model.entity.PartidaEntity;
import com.patota.patota.model.entity.PatotaJogadorEntity;
import com.patota.patota.model.entity.PresencaPartidaEntity;
import com.patota.patota.model.enums.EStatusPresenca;
import com.patota.patota.model.enums.ETipoParticipacaoPartida;
import com.patota.patota.model.repository.PresencaPartidaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PresencaPartidaService {

    private final PresencaPartidaRepository presencaPartidaRepository;

    /** Cria os convites pendentes para os mensalistas ativos da patota. */
    public void criarConvitesPendentes(PartidaEntity partida, List<PatotaJogadorEntity> mensalistas) {
        List<PresencaPartidaEntity> presencas = mensalistas.stream()
                .map(mensalista -> criarPresencaPendente(partida, mensalista))
                .toList();

        presencaPartidaRepository.saveAll(presencas);
    }

    private PresencaPartidaEntity criarPresencaPendente(
            PartidaEntity partida, PatotaJogadorEntity mensalista) {
        PresencaPartidaEntity presenca = new PresencaPartidaEntity();
        presenca.setPartida(partida);
        presenca.setJogador(mensalista.getJogador());
        presenca.setStatus(EStatusPresenca.PENDENTE);
        presenca.setTipoParticipacao(ETipoParticipacaoPartida.MENSALISTA);
        return presenca;
    }
}
