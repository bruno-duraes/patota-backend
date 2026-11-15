package com.patota.patota.model.entity;

import com.patota.patota.model.enums.EStatusParticipante;
import com.patota.patota.model.enums.ETipoJogador;

/** Dados comuns de mensalistas e convidados durante uma partida. */
public interface ParticipanteDaPartida {

    Long getId();

    PartidaEntity getPartida();

    EStatusParticipante getStatus();

    int getNota();

    ETipoJogador getTipo();

    TimePartidaEntity getTimePartida();

    void setTimePartida(TimePartidaEntity timePartida);

    int getGols();

    void setGols(int gols);

    int getAssistencias();

    void setAssistencias(int assistencias);
}
