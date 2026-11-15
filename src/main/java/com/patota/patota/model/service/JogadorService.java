package com.patota.patota.model.service;

import com.patota.patota.model.dto.CriarJogadorDTO;
import com.patota.patota.model.dto.SalvarJogadorDTO;
import com.patota.patota.model.entity.JogadorEntity;
import com.patota.patota.model.repository.JogadorRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JogadorService {

    private final JogadorRepository jogadorRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public JogadorEntity criar(CriarJogadorDTO dados) {
        if (dados == null || dados.senha() == null || dados.senha().length() < 8
                || dados.senha().length() > 72) {
            throw new IllegalArgumentException("A senha deve ter de 8 a 72 caracteres.");
        }
        validar(dados.nome(), dados.email());
        JogadorEntity jogador = new JogadorEntity();
        jogador.setNome(dados.nome().trim());
        jogador.setTelefone(dados.telefone());
        jogador.setEmail(normalizarEmail(dados.email()));
        jogador.setSenhaHash(passwordEncoder.encode(dados.senha()));
        return jogadorRepository.save(jogador);
    }

    @Transactional
    public JogadorEntity atualizar(Long id, SalvarJogadorDTO dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados do jogador são obrigatórios.");
        }
        validar(dados.nome(), dados.email());
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

    public JogadorEntity buscarAutenticado(Authentication autenticacao) {
        return jogadorRepository.findByEmailIgnoreCase(autenticacao.getName())
                .orElseThrow(() -> new EntityNotFoundException("Jogador autenticado não encontrado."));
    }

    public List<JogadorEntity> listar() {
        return jogadorRepository.findAll();
    }

    private void validar(String nome, String email) {
        if (nome == null || nome.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Nome e email do jogador são obrigatórios.");
        }
    }

    private void preencher(JogadorEntity jogador, SalvarJogadorDTO dados) {
        jogador.setNome(dados.nome().trim());
        jogador.setTelefone(dados.telefone());
        jogador.setEmail(normalizarEmail(dados.email()));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
