package com.patota.patota.model.dto;

import com.patota.patota.model.entity.JogadorEntity;
import java.time.LocalDateTime;

public record JogadorRespostaDTO(
        Long id, String nome, String telefone, String email, boolean ativo,
        LocalDateTime criadoEm, LocalDateTime atualizadoEm) {

    public static JogadorRespostaDTO de(JogadorEntity jogador) {
        return new JogadorRespostaDTO(jogador.getId(), jogador.getNome(), jogador.getTelefone(),
                jogador.getEmail(), jogador.isAtivo(), jogador.getCriadoEm(), jogador.getAtualizadoEm());
    }
}
