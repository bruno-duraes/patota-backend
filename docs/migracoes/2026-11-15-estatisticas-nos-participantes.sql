-- Execute com a API parada e após um backup do banco patota.
-- Preserva time, gols e assistências de escalações existentes.

ALTER TABLE jogador_partida
    ADD COLUMN time_partida_id BIGINT NULL,
    ADD COLUMN gols INT NOT NULL DEFAULT 0,
    ADD COLUMN assistencias INT NOT NULL DEFAULT 0;

ALTER TABLE partida_convidado
    ADD COLUMN time_partida_id BIGINT NULL,
    ADD COLUMN gols INT NOT NULL DEFAULT 0,
    ADD COLUMN assistencias INT NOT NULL DEFAULT 0;

UPDATE jogador_partida jp
JOIN escalacoes_partida e ON e.jogador_partida_id = jp.id
SET jp.time_partida_id = e.time_partida_id,
    jp.gols = e.gols,
    jp.assistencias = e.assistencias;

ALTER TABLE jogador_partida
    ADD CONSTRAINT fk_jogador_partida_time
        FOREIGN KEY (time_partida_id) REFERENCES times_partida(id);

ALTER TABLE partida_convidado
    ADD CONSTRAINT fk_partida_convidado_time
        FOREIGN KEY (time_partida_id) REFERENCES times_partida(id);

DROP TABLE escalacoes_partida;
