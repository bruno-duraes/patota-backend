package com.patota.patota.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.patota.patota.model.dto.CriarPartidaDTO;
import com.patota.patota.model.dto.CriarJogadorDTO;
import com.patota.patota.model.dto.SalvarPatotaDTO;
import com.patota.patota.model.entity.JogadorEntity;
import com.patota.patota.model.entity.PatotaEntity;
import com.patota.patota.model.enums.EStatusParticipante;
import com.patota.patota.model.repository.JogadorPartidaRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CadastroPartidaIntegrationTests {

    @Autowired private PatotaService patotaService;
    @Autowired private JogadorService jogadorService;
    @Autowired private PatotaJogadorService patotaJogadorService;
    @Autowired private PartidaService partidaService;
    @Autowired private JogadorPartidaRepository jogadorPartidaRepository;

    @Test
    void partidaConvocaSomenteMensalistasAtivos() {
        PatotaEntity patota = patotaService.criar(new SalvarPatotaDTO("Amigos", null, "Quadra A", null));
        JogadorEntity ativo = jogadorService.criar(
                new CriarJogadorDTO("Ana", null, "ana@teste.com", "senha-segura", null, null));
        JogadorEntity inativo = jogadorService.criar(
                new CriarJogadorDTO("Bia", null, "bia@teste.com", "senha-segura", null, null));
        patotaJogadorService.adicionarMensalista(patota.getId(), ativo.getId());
        patotaJogadorService.adicionarMensalista(patota.getId(), inativo.getId());
        jogadorService.definirAtivo(inativo.getId(), false);

        var partida = partidaService.criar(new CriarPartidaDTO(
                patota.getId(), LocalDateTime.now().plusDays(7), null));

        var presencas = jogadorPartidaRepository.findAll();
        assertEquals(1, presencas.size());
        assertEquals(partida.getId(), presencas.getFirst().getPartida().getId());
        assertEquals(ativo.getId(), presencas.getFirst().getJogador().getId());
        assertEquals(EStatusParticipante.PENDENTE, presencas.getFirst().getStatus());
    }
}
