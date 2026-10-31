package com.patota.patota.model.service;

import com.patota.patota.model.dto.SalvarJogadorDTO;
import com.patota.patota.model.entity.JogadorEntity;
import com.patota.patota.model.repository.JogadorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JogadorService {

    private final JogadorRepository jogadorRepository;

    @Transactional
    public JogadorEntity criar(SalvarJogadorDTO dados) {
        validar(dados);
        JogadorEntity jogador = new JogadorEntity();
        preencher(jogador, dados);
        return jogadorRepository.save(jogador);
    }

    @Transactional
    public JogadorEntity atualizar(Long id, SalvarJogadorDTO dados) {
        validar(dados);
        JogadorEntity jogador = buscar(id);
        preencher(jogador, dados);
        return jogadorRepository.save(jogador);
    }

    @Transactional
    public JogadorEntity definirAtivo(Long id, boolean ativo) {
        JogadorEntity jogador = buscar(id);
        jogador.setAtivo(ativo);
        return jogadorRepository.save(jogador);
    }

    public JogadorEntity buscar(Long id) {
        return jogadorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Jogador não encontrado."));
    }

    private void validar(SalvarJogadorDTO dados) {
        if (dados == null || dados.nome() == null || dados.nome().isBlank()) {
            throw new IllegalArgumentException("Nome do jogador é obrigatório.");
        }
    }

    private void preencher(JogadorEntity jogador, SalvarJogadorDTO dados) {
        jogador.setNome(dados.nome().trim());
        jogador.setTelefone(dados.telefone());
        jogador.setEmail(dados.email());
    }
}
