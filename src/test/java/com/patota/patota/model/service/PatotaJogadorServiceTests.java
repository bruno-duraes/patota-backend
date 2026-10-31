package com.patota.patota.model.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.patota.patota.model.entity.JogadorEntity;
import com.patota.patota.model.entity.PatotaEntity;
import com.patota.patota.model.entity.PatotaJogadorEntity;
import com.patota.patota.model.repository.PatotaJogadorRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PatotaJogadorServiceTests {

    @Mock private PatotaJogadorRepository repository;
    @Mock private PatotaService patotaService;
    @Mock private JogadorService jogadorService;
    private PatotaJogadorService service;

    @BeforeEach
    void preparar() {
        MockitoAnnotations.openMocks(this);
        service = new PatotaJogadorService(repository, patotaService, jogadorService);
    }

    @Test
    void reativaVinculoExistenteSemCriarDuplicata() {
        PatotaEntity patota = new PatotaEntity();
        JogadorEntity jogador = new JogadorEntity();
        PatotaJogadorEntity vinculo = new PatotaJogadorEntity();
        vinculo.setAtivo(false);
        vinculo.setSaiuEm(java.time.LocalDateTime.now());
        when(patotaService.buscarPatota(1L)).thenReturn(patota);
        when(jogadorService.buscar(2L)).thenReturn(jogador);
        when(repository.findByPatota_IdAndJogador_Id(1L, 2L)).thenReturn(Optional.of(vinculo));
        when(repository.save(vinculo)).thenReturn(vinculo);

        PatotaJogadorEntity resultado = service.adicionarMensalista(1L, 2L);

        assertTrue(resultado.isAtivo());
        assertNull(resultado.getSaiuEm());
        verify(repository).save(vinculo);
    }

    @Test
    void rejeitaVinculoDuplicado() {
        when(patotaService.buscarPatota(1L)).thenReturn(new PatotaEntity());
        when(jogadorService.buscar(2L)).thenReturn(new JogadorEntity());
        when(repository.findByPatota_IdAndJogador_Id(1L, 2L))
                .thenReturn(Optional.of(new PatotaJogadorEntity()));

        assertThrows(IllegalStateException.class, () -> service.adicionarMensalista(1L, 2L));
    }

    @Test
    void desativaVinculoERegistraSaida() {
        PatotaJogadorEntity vinculo = new PatotaJogadorEntity();
        when(repository.findByPatota_IdAndJogador_Id(1L, 2L)).thenReturn(Optional.of(vinculo));
        when(repository.save(vinculo)).thenReturn(vinculo);

        service.removerMensalista(1L, 2L);

        assertFalse(vinculo.isAtivo());
        assertTrue(vinculo.getSaiuEm() != null);
    }

    @Test
    void buscaSomenteMensalistasComJogadorAtivo() {
        when(repository.findAllByPatota_IdAndAtivoTrueAndJogador_AtivoTrue(1L))
                .thenReturn(List.of());
        assertTrue(service.buscarMensalistasAtivosDaPatota(1L).isEmpty());
        verify(repository).findAllByPatota_IdAndAtivoTrueAndJogador_AtivoTrue(1L);
    }
}
