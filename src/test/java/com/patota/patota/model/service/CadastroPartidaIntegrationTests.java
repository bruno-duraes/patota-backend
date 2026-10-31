package com.patota.patota.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.patota.patota.model.dto.CriarPartidaDTO;
import com.patota.patota.model.dto.SalvarJogadorDTO;
import com.patota.patota.model.dto.SalvarPatotaDTO;
import com.patota.patota.model.entity.JogadorEntity;
import com.patota.patota.model.entity.PatotaEntity;
import com.patota.patota.model.enums.EStatusPresenca;
import com.patota.patota.model.repository.PresencaPartidaRepository;
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
    @Autowired private PresencaPartidaRepository presencaPartidaRepository;

    @Test
    void partidaConvocaSomenteMensalistasAtivos() {
        PatotaEntity patota = patotaService.criar(new SalvarPatotaDTO("Amigos", null, "Quadra A"));
        JogadorEntity ativo = jogadorService.criar(new SalvarJogadorDTO("Ana", null, null));
        JogadorEntity inativo = jogadorService.criar(new SalvarJogadorDTO("Bia", null, null));
        patotaJogadorService.adicionarMensalista(patota.getId(), ativo.getId());
        patotaJogadorService.adicionarMensalista(patota.getId(), inativo.getId());
        jogadorService.definirAtivo(inativo.getId(), false);

        var partida = partidaService.criar(new CriarPartidaDTO(
                patota.getId(), LocalDateTime.now().plusDays(7), null));

        var presencas = presencaPartidaRepository.findAll();
        assertEquals(1, presencas.size());
        assertEquals(partida.getId(), presencas.getFirst().getPartida().getId());
        assertEquals(ativo.getId(), presencas.getFirst().getJogador().getId());
        assertEquals(EStatusPresenca.PENDENTE, presencas.getFirst().getStatus());
    }
}
