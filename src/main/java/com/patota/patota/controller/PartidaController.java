package com.patota.patota.controller;

import com.patota.patota.config.RequerGerente;
import com.patota.patota.model.dto.CriarPartidaDTO;
import com.patota.patota.model.dto.ConvidarJogadorDTO;
import com.patota.patota.model.dto.ParticipantesPartidaRespostaDTO;
import com.patota.patota.model.dto.PartidaRespostaDTO;
import com.patota.patota.model.dto.PartidaConvidadoRespostaDTO;
import com.patota.patota.model.service.ParticipantePartidaService;
import com.patota.patota.model.service.JogadorService;
import com.patota.patota.model.service.PartidaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/partidas")
@RequiredArgsConstructor
public class PartidaController {

    private final PartidaService partidaService;
    private final ParticipantePartidaService participantePartidaService;
    private final JogadorService jogadorService;

    @PostMapping
    @RequerGerente("#dados.patotaId()")
    public ResponseEntity<PartidaRespostaDTO> criar(
            @Valid @RequestBody CriarPartidaDTO dados) {
        PartidaRespostaDTO resposta = PartidaRespostaDTO.de(partidaService.criar(dados));
        return ResponseEntity.created(URI.create("/api/partidas/" + resposta.id())).body(resposta);
    }

    @GetMapping("/{id}")
    public PartidaRespostaDTO buscar(@PathVariable Long id) {
        return PartidaRespostaDTO.de(partidaService.buscar(id));
    }

    @GetMapping
    public List<PartidaRespostaDTO> listarDaPatota(@RequestParam Long patotaId) {
        return partidaService.listarDaPatota(patotaId).stream().map(PartidaRespostaDTO::de).toList();
    }

    @PostMapping("/{id}/convidados")
    public ResponseEntity<PartidaConvidadoRespostaDTO> convidar(
            @PathVariable Long id, @Valid @RequestBody ConvidarJogadorDTO dados,
            Authentication autenticacao) {
        Long convidadorId = jogadorService.buscarAutenticado(autenticacao).getId();
        PartidaConvidadoRespostaDTO resposta = participantePartidaService.convidar(id, dados, convidadorId);
        return ResponseEntity.created(URI.create("/api/partidas/" + id + "/convidados/" + resposta.id()))
                .body(resposta);
    }

    @GetMapping("/{id}/participantes")
    public ParticipantesPartidaRespostaDTO listarParticipantes(@PathVariable Long id) {
        return participantePartidaService.listar(id);
    }

    @GetMapping("/{id}/convidados/{convidadoId}")
    public PartidaConvidadoRespostaDTO buscarConvidado(
            @PathVariable Long id, @PathVariable Long convidadoId) {
        return participantePartidaService.buscarConvidado(id, convidadoId);
    }

    @PatchMapping("/{id}/convidados/{convidadoId}/aprovar")
    @RequerGerente("@autorizacaoGerente.patotaDaPartida(#id)")
    public PartidaConvidadoRespostaDTO aprovar(
            @PathVariable Long id, @PathVariable Long convidadoId, Authentication autenticacao) {
        Long gerenteId = jogadorService.buscarAutenticado(autenticacao).getId();
        return participantePartidaService.aprovar(id, convidadoId, gerenteId);
    }

    @PatchMapping("/{id}/convidados/{convidadoId}/recusar")
    @RequerGerente("@autorizacaoGerente.patotaDaPartida(#id)")
    public PartidaConvidadoRespostaDTO recusar(
            @PathVariable Long id, @PathVariable Long convidadoId, Authentication autenticacao) {
        Long gerenteId = jogadorService.buscarAutenticado(autenticacao).getId();
        return participantePartidaService.recusar(id, convidadoId, gerenteId);
    }
}
