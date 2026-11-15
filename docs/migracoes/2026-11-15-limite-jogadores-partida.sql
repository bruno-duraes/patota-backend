-- Execute com a API parada. Patotas existentes recebem limite 12.
-- Partidas existentes recebem uma cópia do limite atual da respectiva patota.

ALTER TABLE patotas
    ADD COLUMN maximo_jogadores_por_partida INT NOT NULL DEFAULT 12,
    ADD CONSTRAINT ck_patotas_maximo_jogadores
        CHECK (maximo_jogadores_por_partida BETWEEN 2 AND 100);

ALTER TABLE partidas
    ADD COLUMN maximo_jogadores INT NOT NULL DEFAULT 12,
    ADD CONSTRAINT ck_partidas_maximo_jogadores
        CHECK (maximo_jogadores BETWEEN 2 AND 100);

UPDATE partidas partida
JOIN patotas patota ON patota.id = partida.patota_id
SET partida.maximo_jogadores = patota.maximo_jogadores_por_partida;
