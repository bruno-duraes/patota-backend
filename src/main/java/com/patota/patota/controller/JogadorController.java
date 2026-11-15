package com.patota.patota.controller;

import com.patota.patota.model.dto.CriarJogadorDTO;
import com.patota.patota.model.dto.JogadorRespostaDTO;
import com.patota.patota.model.dto.SalvarJogadorDTO;
import com.patota.patota.model.service.JogadorService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jogadores")
@RequiredArgsConstructor
public class JogadorController {

    private final JogadorService jogadorService;

    @PostMapping
    public ResponseEntity<JogadorRespostaDTO> criar(@Valid @RequestBody CriarJogadorDTO dados) {
        JogadorRespostaDTO resposta = JogadorRespostaDTO.de(jogadorService.criar(dados));
        return ResponseEntity.created(URI.create("/api/jogadores/" + resposta.id())).body(resposta);
    }

    @GetMapping
    public List<JogadorRespostaDTO> listar() {
        return jogadorService.listar().stream().map(JogadorRespostaDTO::de).toList();
    }

    @GetMapping("/{id}")
    public JogadorRespostaDTO buscar(@PathVariable Long id) {
        return JogadorRespostaDTO.de(jogadorService.buscar(id));
    }

    @PutMapping("/{id}")
    public JogadorRespostaDTO atualizar(
            @PathVariable Long id, @Valid @RequestBody SalvarJogadorDTO dados,
            Authentication autenticacao) {
        exigirProprioJogador(id, autenticacao);
        return JogadorRespostaDTO.de(jogadorService.atualizar(id, dados));
    }

    @PatchMapping("/{id}/ativar")
    public JogadorRespostaDTO ativar(@PathVariable Long id, Authentication autenticacao) {
        exigirProprioJogador(id, autenticacao);
        return JogadorRespostaDTO.de(jogadorService.definirAtivo(id, true));
    }

    @PatchMapping("/{id}/desativar")
    public JogadorRespostaDTO desativar(@PathVariable Long id, Authentication autenticacao) {
        exigirProprioJogador(id, autenticacao);
        return JogadorRespostaDTO.de(jogadorService.definirAtivo(id, false));
    }

    private void exigirProprioJogador(Long id, Authentication autenticacao) {
        if (!jogadorService.buscarAutenticado(autenticacao).getId().equals(id)) {
            throw new AccessDeniedException("O jogador só pode alterar o próprio cadastro.");
        }
    }
}
