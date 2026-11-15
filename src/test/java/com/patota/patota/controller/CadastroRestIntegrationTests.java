package com.patota.patota.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private long cadastrar(String nome, String email) throws Exception {
        String location = mvc.perform(post("/api/jogadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email
                                + "\",\"senha\":\"Senha1234\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        return idDe(location);
    }

    private long idDe(String location) {
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }
}
