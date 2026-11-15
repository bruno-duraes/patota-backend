package com.patota.patota.controller;

import com.patota.patota.config.RequerGerente;
import com.patota.patota.model.dto.PatotaRespostaDTO;
import com.patota.patota.model.dto.SalvarPatotaDTO;
import com.patota.patota.model.service.PatotaService;
import com.patota.patota.model.service.JogadorService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/patotas")
@RequiredArgsConstructor
public class PatotaController {

    private final PatotaService patotaService;
    private final JogadorService jogadorService;

    @PostMapping
    public ResponseEntity<PatotaRespostaDTO> criar(
            @Valid @RequestBody SalvarPatotaDTO dados, Authentication autenticacao) {
        Long gerenteId = jogadorService.buscarAutenticado(autenticacao).getId();
        PatotaRespostaDTO resposta = PatotaRespostaDTO.de(patotaService.criarGerenciada(dados, gerenteId));
        return ResponseEntity.created(URI.create("/api/patotas/" + resposta.id())).body(resposta);
    }

    @GetMapping
    public List<PatotaRespostaDTO> listar() {
        return patotaService.listar().stream().map(PatotaRespostaDTO::de).toList();
    }

    @GetMapping("/{id}")
    public PatotaRespostaDTO buscar(@PathVariable Long id) {
        return PatotaRespostaDTO.de(patotaService.buscarPatota(id));
    }

    @PutMapping("/{id}")
    @RequerGerente("#id")
    public PatotaRespostaDTO atualizar(
            @PathVariable Long id, @Valid @RequestBody SalvarPatotaDTO dados) {
        return PatotaRespostaDTO.de(patotaService.atualizar(id, dados));
    }

    @PatchMapping("/{id}/ativar")
    @RequerGerente("#id")
    public PatotaRespostaDTO ativar(@PathVariable Long id) {
        return PatotaRespostaDTO.de(patotaService.definirAtiva(id, true));
    }

    @PatchMapping("/{id}/desativar")
    @RequerGerente("#id")
    public PatotaRespostaDTO desativar(@PathVariable Long id) {
        return PatotaRespostaDTO.de(patotaService.definirAtiva(id, false));
    }
}
