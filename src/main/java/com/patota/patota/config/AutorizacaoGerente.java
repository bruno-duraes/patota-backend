package com.patota.patota.config;

import com.patota.patota.model.enums.ECargoPatota;
import com.patota.patota.model.repository.JogadorRepository;
import com.patota.patota.model.repository.PartidaRepository;
import com.patota.patota.model.repository.PatotaJogadorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("autorizacaoGerente")
@RequiredArgsConstructor
public class AutorizacaoGerente {

    private final JogadorRepository jogadores;
    private final PatotaJogadorRepository vinculos;
    private final PartidaRepository partidas;

    public boolean podeAdministrar(Long patotaId, Authentication autenticacao) {
        if (patotaId == null || autenticacao == null || !autenticacao.isAuthenticated()) {
            return false;
        }
        return jogadores.findByEmailIgnoreCase(autenticacao.getName())
                .filter(jogador -> jogador.isAtivo() && vinculos
                        .existsByPatota_IdAndJogador_IdAndAtivoTrueAndCargo(
                                patotaId, jogador.getId(), ECargoPatota.GERENTE))
                .isPresent();
    }

    public Long patotaDaPartida(Long partidaId) {
        return partidas.findById(partidaId)
                .map(partida -> partida.getPatota().getId())
                .orElseThrow(() -> new EntityNotFoundException("Partida não encontrada."));
    }
}
