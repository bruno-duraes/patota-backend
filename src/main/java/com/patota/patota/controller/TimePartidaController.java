package com.patota.patota.controller;

import com.patota.patota.config.RequerGerente;
import com.patota.patota.model.dto.AtualizarEstatisticasDTO;
import com.patota.patota.model.dto.CriarTimePartidaDTO;
import com.patota.patota.model.dto.DefinirTimeParticipanteDTO;
import com.patota.patota.model.dto.ParticipanteTimeRespostaDTO;
import com.patota.patota.model.dto.TimePartidaRespostaDTO;
import com.patota.patota.model.enums.ETipoParticipantePartida;
import com.patota.patota.model.service.MontagemTimesService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/partidas/{partidaId}")
@RequiredArgsConstructor
public class TimePartidaController {

    private final MontagemTimesService montagemTimesService;

    @PostMapping("/times")
    @RequerGerente("@autorizacaoGerente.patotaDaPartida(#partidaId)")
    public ResponseEntity<TimePartidaRespostaDTO> criar(
            @PathVariable Long partidaId, @Valid @RequestBody CriarTimePartidaDTO dados) {
        TimePartidaRespostaDTO resposta = montagemTimesService.criar(partidaId, dados);
        return ResponseEntity.created(URI.create("/api/partidas/" + partidaId + "/times/" + resposta.id()))
                .body(resposta);
    }

    @GetMapping("/times")
    public List<TimePartidaRespostaDTO> listar(@PathVariable Long partidaId) {
        return montagemTimesService.listar(partidaId);
    }

    @PostMapping("/times/balancear")
    @RequerGerente("@autorizacaoGerente.patotaDaPartida(#partidaId)")
    public List<TimePartidaRespostaDTO> balancear(@PathVariable Long partidaId) {
        return montagemTimesService.balancear(partidaId);
    }

    @PatchMapping("/participantes/{tipo}/{participanteId}/time")
    @RequerGerente("@autorizacaoGerente.patotaDaPartida(#partidaId)")
    public ParticipanteTimeRespostaDTO atribuir(
            @PathVariable Long partidaId, @PathVariable ETipoParticipantePartida tipo,
            @PathVariable Long participanteId, @Valid @RequestBody DefinirTimeParticipanteDTO dados) {
        return montagemTimesService.atribuir(partidaId, tipo, participanteId, dados.timeId());
    }

    @DeleteMapping("/participantes/{tipo}/{participanteId}/time")
    @RequerGerente("@autorizacaoGerente.patotaDaPartida(#partidaId)")
    public ParticipanteTimeRespostaDTO removerDoTime(
            @PathVariable Long partidaId, @PathVariable ETipoParticipantePartida tipo,
            @PathVariable Long participanteId) {
        return montagemTimesService.removerDoTime(partidaId, tipo, participanteId);
    }

    @PatchMapping("/participantes/{tipo}/{participanteId}/estatisticas")
    @RequerGerente("@autorizacaoGerente.patotaDaPartida(#partidaId)")
    public ParticipanteTimeRespostaDTO atualizarEstatisticas(
            @PathVariable Long partidaId, @PathVariable ETipoParticipantePartida tipo,
            @PathVariable Long participanteId, @Valid @RequestBody AtualizarEstatisticasDTO dados) {
        return montagemTimesService.atualizarEstatisticas(partidaId, tipo, participanteId, dados);
    }
}
