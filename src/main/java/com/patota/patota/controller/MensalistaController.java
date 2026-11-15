package com.patota.patota.controller;

import com.patota.patota.config.RequerGerente;
import com.patota.patota.model.dto.AdicionarMensalistaDTO;
import com.patota.patota.model.dto.DefinirCargoDTO;
import com.patota.patota.model.dto.MensalistaRespostaDTO;
import com.patota.patota.model.service.JogadorService;
import com.patota.patota.model.service.PatotaJogadorService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patotas/{patotaId}/mensalistas")
@RequiredArgsConstructor
public class MensalistaController {

    private final PatotaJogadorService patotaJogadorService;
    private final JogadorService jogadorService;

    @PostMapping
    @RequerGerente("#patotaId")
    public ResponseEntity<MensalistaRespostaDTO> adicionar(
            @PathVariable Long patotaId, @Valid @RequestBody AdicionarMensalistaDTO dados) {
        MensalistaRespostaDTO resposta = MensalistaRespostaDTO.de(
                patotaJogadorService.adicionarMensalista(patotaId, dados.jogadorId()));
        return ResponseEntity.created(URI.create(
                "/api/patotas/" + patotaId + "/mensalistas/" + dados.jogadorId())).body(resposta);
    }

    @GetMapping
    public List<MensalistaRespostaDTO> listar(@PathVariable Long patotaId) {
        return patotaJogadorService.buscarMensalistasAtivosDaPatota(patotaId).stream()
                .map(MensalistaRespostaDTO::de).toList();
    }

    @DeleteMapping("/{jogadorId}")
    @RequerGerente("#patotaId")
    public MensalistaRespostaDTO remover(
            @PathVariable Long patotaId, @PathVariable Long jogadorId) {
        return MensalistaRespostaDTO.de(patotaJogadorService.removerMensalista(patotaId, jogadorId));
    }

    @PatchMapping("/{jogadorId}/cargo")
    @RequerGerente("#patotaId")
    public MensalistaRespostaDTO definirCargo(
            @PathVariable Long patotaId, @PathVariable Long jogadorId,
            @Valid @RequestBody DefinirCargoDTO dados, Authentication autenticacao) {
        Long gerenteId = jogadorService.buscarAutenticado(autenticacao).getId();
        return MensalistaRespostaDTO.de(
                patotaJogadorService.definirCargo(patotaId, jogadorId, gerenteId, dados.cargo()));
    }
}
