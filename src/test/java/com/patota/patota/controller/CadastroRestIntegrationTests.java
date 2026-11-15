package com.patota.patota.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.patota.patota.model.repository.JogadorPartidaRepository;
import com.patota.patota.model.repository.JogadorRepository;
import com.patota.patota.model.repository.PartidaConvidadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Transactional
class CadastroRestIntegrationTests {

    @Autowired private WebApplicationContext contexto;
    @Autowired private JogadorPartidaRepository jogadorPartidaRepository;
    @Autowired private PartidaConvidadoRepository partidaConvidadoRepository;
    @Autowired private JogadorRepository jogadorRepository;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void gerenteAdministraPatotaEAprovaConvidadoSemCadastrarNovoJogador() throws Exception {
        long anaId = cadastrar("Ana", "ana@teste.com");
        long biaId = cadastrar("Bia", "bia@teste.com");

        String patotaLocation = mvc.perform(post("/api/patotas")
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Amigos\",\"local\":\"Quadra A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        long patotaId = idDe(patotaLocation);

        mvc.perform(get("/api/patotas/{id}/mensalistas", patotaId)
                        .with(httpBasic("ana@teste.com", "Senha1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jogadorId").value(anaId))
                .andExpect(jsonPath("$[0].cargo").value("GERENTE"));

        mvc.perform(patch("/api/patotas/{id}/mensalistas/{jogadorId}/cargo", patotaId, anaId)
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cargo\":\"PARTICIPANTE\"}"))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/patotas/{id}/mensalistas", patotaId)
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jogadorId\":" + biaId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cargo").value("PARTICIPANTE"));

        mvc.perform(patch("/api/patotas/{id}/desativar", patotaId)
                        .with(httpBasic("bia@teste.com", "Senha1234")))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/patotas/{id}/mensalistas/{jogadorId}/cargo", patotaId, biaId)
                        .with(httpBasic("bia@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cargo\":\"GERENTE\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/partidas")
                        .with(httpBasic("bia@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patotaId\":" + patotaId
                                + ",\"dataHora\":\"2026-11-20T20:00:00\"}"))
                .andExpect(status().isForbidden());

        long outraPatotaId = idDe(mvc.perform(post("/api/patotas")
                        .with(httpBasic("bia@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Outra\",\"local\":\"Quadra B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location"));
        mvc.perform(patch("/api/patotas/{id}/desativar", patotaId)
                        .with(httpBasic("bia@teste.com", "Senha1234")))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/patotas/{id}/desativar", outraPatotaId)
                        .with(httpBasic("bia@teste.com", "Senha1234")))
                .andExpect(status().isOk());

        String partidaLocation = mvc.perform(post("/api/partidas")
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patotaId\":" + patotaId
                                + ",\"dataHora\":\"2026-11-20T20:00:00\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        long partidaId = idDe(partidaLocation);

        String convidadoLocation = mvc.perform(post("/api/partidas/{id}/convidados", partidaId)
                        .with(httpBasic("bia@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Carlos\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.convidadoPorJogadorId").value(biaId))
                .andReturn().getResponse().getHeader("Location");
        long convidadoId = idDe(convidadoLocation);

        mvc.perform(patch("/api/partidas/{id}/convidados/{convidadoId}/aprovar", partidaId, convidadoId)
                        .with(httpBasic("bia@teste.com", "Senha1234")))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/partidas/{id}/convidados/{convidadoId}/aprovar", partidaId, convidadoId)
                        .with(httpBasic("ana@teste.com", "Senha1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));

        mvc.perform(post("/api/partidas/{id}/times", partidaId)
                        .with(httpBasic("bia@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Azul\"}"))
                .andExpect(status().isForbidden());
        long timeId = idDe(mvc.perform(post("/api/partidas/{id}/times", partidaId)
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Azul\",\"cor\":\"Azul\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location"));

        mvc.perform(patch("/api/partidas/{id}/participantes/CONVIDADO/{convidadoId}/time", partidaId, convidadoId)
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"timeId\":" + timeId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeId").value(timeId));
        mvc.perform(patch("/api/partidas/{id}/participantes/CONVIDADO/{convidadoId}/estatisticas", partidaId, convidadoId)
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gols\":2,\"assistencias\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gols").value(2))
                .andExpect(jsonPath("$.assistencias").value(1));

        var mensalista = jogadorPartidaRepository.findAllByPartida_IdOrderById(partidaId).stream()
                .filter(p -> p.getJogador().getId().equals(biaId)).findFirst().orElseThrow();
        mvc.perform(patch("/api/partidas/{id}/participantes/MENSALISTA/{participanteId}/time", partidaId, mensalista.getId())
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"timeId\":" + timeId + "}"))
                .andExpect(status().isConflict());
        mvc.perform(patch("/api/partidas/{id}/mensalistas/{participanteId}/confirmar", partidaId, mensalista.getId())
                        .with(httpBasic("ana@teste.com", "Senha1234")))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/partidas/{id}/mensalistas/{participanteId}/confirmar", partidaId, mensalista.getId())
                        .with(httpBasic("bia@teste.com", "Senha1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));
        mvc.perform(patch("/api/partidas/{id}/participantes/MENSALISTA/{participanteId}/time", partidaId, mensalista.getId())
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"timeId\":" + timeId + "}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/partidas/{id}/participantes/MENSALISTA/{participanteId}/estatisticas", partidaId, mensalista.getId())
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gols\":1,\"assistencias\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gols").value(1));
        mvc.perform(get("/api/partidas/{id}/times", partidaId)
                        .with(httpBasic("ana@teste.com", "Senha1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].participantes.length()").value(2))
                .andExpect(jsonPath("$[0].participantes[0].tipo").value("MENSALISTA"))
                .andExpect(jsonPath("$[0].participantes[1].tipo").value("CONVIDADO"));

        mvc.perform(get("/api/partidas/{id}/participantes", partidaId)
                        .with(httpBasic("ana@teste.com", "Senha1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensalistas.length()").value(2))
                .andExpect(jsonPath("$.convidados[0].nome").value("Carlos"))
                .andExpect(jsonPath("$.convidados[0].status").value("CONFIRMADO"));

        mvc.perform(patch("/api/patotas/{id}/mensalistas/{jogadorId}/cargo", patotaId, biaId)
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cargo\":\"GERENTE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargo").value("GERENTE"));
        mvc.perform(patch("/api/patotas/{id}/mensalistas/{jogadorId}/cargo", patotaId, anaId)
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cargo\":\"PARTICIPANTE\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/patotas/{id}/desativar", patotaId)
                        .with(httpBasic("ana@teste.com", "Senha1234")))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/patotas/{id}/desativar", patotaId)
                        .with(httpBasic("bia@teste.com", "Senha1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativa").value(false));

        assertEquals(2, jogadorPartidaRepository.count());
        assertEquals(1, partidaConvidadoRepository.count());
        assertEquals(2, jogadorRepository.count());
    }

    @Test
    void exigeCredenciaisEValidaCadastro() throws Exception {
        mvc.perform(get("/api/patotas"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/jogadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@teste.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void balanceiaTimesComUmGoleiroPorTimeENotasDeMensalistasEConvidados() throws Exception {
        cadastrar("Ana", "ana@teste.com", 5, "GOLEIRO");
        long biaId = cadastrar("Bia", "bia@teste.com", 2, "GOLEIRO");
        long caioId = cadastrar("Caio", "caio@teste.com", 5, "LINHA");
        long daniId = cadastrar("Dani", "dani@teste.com", 3, "LINHA");
        long patotaId = idDe(mvc.perform(post("/api/patotas")
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Amigos\",\"local\":\"Quadra A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location"));
        for (long jogadorId : new long[]{biaId, caioId, daniId}) {
            mvc.perform(post("/api/patotas/{id}/mensalistas", patotaId)
                            .with(httpBasic("ana@teste.com", "Senha1234"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"jogadorId\":" + jogadorId + "}"))
                    .andExpect(status().isCreated());
        }
        long partidaId = idDe(mvc.perform(post("/api/partidas")
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patotaId\":" + patotaId
                                + ",\"dataHora\":\"2026-11-20T20:00:00\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location"));
        long convidadoId = idDe(mvc.perform(post("/api/partidas/{id}/convidados", partidaId)
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Eva\",\"nota\":4,\"tipo\":\"LINHA\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nota").value(4))
                .andReturn().getResponse().getHeader("Location"));
        mvc.perform(patch("/api/partidas/{id}/convidados/{convidadoId}/aprovar", partidaId, convidadoId)
                        .with(httpBasic("ana@teste.com", "Senha1234")))
                .andExpect(status().isOk());
        for (var participante : jogadorPartidaRepository.findAllByPartida_IdOrderById(partidaId)) {
            if (participante.getJogador().getId().equals(biaId)) {
                continue;
            }
            String email = participante.getJogador().getEmail();
            mvc.perform(patch("/api/partidas/{id}/mensalistas/{participanteId}/confirmar",
                            partidaId, participante.getId()).with(httpBasic(email, "Senha1234")))
                    .andExpect(status().isOk());
        }
        mvc.perform(post("/api/partidas/{id}/times", partidaId)
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Azul\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/partidas/{id}/times", partidaId)
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Vermelho\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/partidas/{id}/times/balancear", partidaId)
                        .with(httpBasic("ana@teste.com", "Senha1234")))
                .andExpect(status().isConflict());
        var biaNaPartida = jogadorPartidaRepository.findAllByPartida_IdOrderById(partidaId).stream()
                .filter(p -> p.getJogador().getId().equals(biaId)).findFirst().orElseThrow();
        mvc.perform(patch("/api/partidas/{id}/mensalistas/{participanteId}/confirmar",
                        partidaId, biaNaPartida.getId()).with(httpBasic("bia@teste.com", "Senha1234")))
                .andExpect(status().isOk());
        mvc.perform(post("/api/partidas/{id}/times/balancear", partidaId)
                        .with(httpBasic("ana@teste.com", "Senha1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].participantes.length()").value(2))
                .andExpect(jsonPath("$[0].participantes[0].tipoJogador").value("GOLEIRO"))
                .andExpect(jsonPath("$[0].participantes[0].nota").value(5))
                .andExpect(jsonPath("$[0].participantes[1].nome").value("Eva"))
                .andExpect(jsonPath("$[1].participantes.length()").value(3))
                .andExpect(jsonPath("$[1].participantes[0].tipoJogador").value("GOLEIRO"))
                .andExpect(jsonPath("$[1].participantes[0].nota").value(2));
    }

    @Test
    void limitaConfirmadosSomandoMensalistasEConvidados() throws Exception {
        long anaId = cadastrar("Ana", "ana@teste.com");
        long biaId = cadastrar("Bia", "bia@teste.com");
        long caioId = cadastrar("Caio", "caio@teste.com");
        long patotaId = idDe(mvc.perform(post("/api/patotas")
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Amigos\",\"local\":\"Quadra A\",\"maximoJogadoresPorPartida\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maximoJogadoresPorPartida").value(2))
                .andReturn().getResponse().getHeader("Location"));
        for (long jogadorId : new long[]{biaId, caioId}) {
            mvc.perform(post("/api/patotas/{id}/mensalistas", patotaId)
                            .with(httpBasic("ana@teste.com", "Senha1234"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"jogadorId\":" + jogadorId + "}"))
                    .andExpect(status().isCreated());
        }
        long partidaId = idDe(mvc.perform(post("/api/partidas")
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patotaId\":" + patotaId
                                + ",\"dataHora\":\"2026-11-20T20:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maximoJogadores").value(2))
                .andReturn().getResponse().getHeader("Location"));

        for (long jogadorId : new long[]{anaId, biaId}) {
            var participante = jogadorPartidaRepository.findAllByPartida_IdOrderById(partidaId).stream()
                    .filter(p -> p.getJogador().getId().equals(jogadorId)).findFirst().orElseThrow();
            String email = participante.getJogador().getEmail();
            mvc.perform(patch("/api/partidas/{id}/mensalistas/{participanteId}/confirmar",
                            partidaId, participante.getId()).with(httpBasic(email, "Senha1234")))
                    .andExpect(status().isOk());
        }
        var caioNaPartida = jogadorPartidaRepository.findAllByPartida_IdOrderById(partidaId).stream()
                .filter(p -> p.getJogador().getId().equals(caioId)).findFirst().orElseThrow();
        mvc.perform(patch("/api/partidas/{id}/mensalistas/{participanteId}/confirmar",
                        partidaId, caioNaPartida.getId()).with(httpBasic("caio@teste.com", "Senha1234")))
                .andExpect(status().isConflict());

        long convidadoId = idDe(mvc.perform(post("/api/partidas/{id}/convidados", partidaId)
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Duda\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location"));
        mvc.perform(patch("/api/partidas/{id}/convidados/{convidadoId}/aprovar", partidaId, convidadoId)
                        .with(httpBasic("ana@teste.com", "Senha1234")))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/partidas/{id}/convidados/{convidadoId}", partidaId, convidadoId)
                        .with(httpBasic("ana@teste.com", "Senha1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        mvc.perform(put("/api/patotas/{id}", patotaId)
                        .with(httpBasic("ana@teste.com", "Senha1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Amigos\",\"local\":\"Quadra A\",\"maximoJogadoresPorPartida\":3}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/partidas/{id}/mensalistas/{participanteId}/confirmar",
                        partidaId, caioNaPartida.getId()).with(httpBasic("caio@teste.com", "Senha1234")))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/partidas/{id}", partidaId)
                        .with(httpBasic("ana@teste.com", "Senha1234")))
                .andExpect(jsonPath("$.maximoJogadores").value(2));
    }

    private long cadastrar(String nome, String email) throws Exception {
        String location = mvc.perform(post("/api/jogadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email
                                + "\",\"senha\":\"Senha1234\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        return idDe(location);
    }

    private long cadastrar(String nome, String email, int nota, String tipo) throws Exception {
        String location = mvc.perform(post("/api/jogadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email
                                + "\",\"senha\":\"Senha1234\",\"nota\":" + nota
                                + ",\"tipo\":\"" + tipo + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nota").value(nota))
                .andExpect(jsonPath("$.tipo").value(tipo))
                .andReturn().getResponse().getHeader("Location");
        return idDe(location);
    }

    private long idDe(String location) {
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }
}
