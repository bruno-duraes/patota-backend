-- Execute com a API parada. Cadastros existentes recebem nota 3 e tipo LINHA.

ALTER TABLE jogadores
    ADD COLUMN nota INT NOT NULL DEFAULT 3,
    ADD COLUMN tipo ENUM('LINHA', 'GOLEIRO') NOT NULL DEFAULT 'LINHA',
    ADD CONSTRAINT ck_jogadores_nota CHECK (nota BETWEEN 1 AND 5);

ALTER TABLE partida_convidado
    ADD COLUMN nota INT NOT NULL DEFAULT 3,
    ADD COLUMN tipo ENUM('LINHA', 'GOLEIRO') NOT NULL DEFAULT 'LINHA',
    ADD CONSTRAINT ck_partida_convidado_nota CHECK (nota BETWEEN 1 AND 5);
