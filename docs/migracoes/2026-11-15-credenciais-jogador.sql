-- Execute com a API parada. No banco local de desenvolvimento não havia jogadores.
-- Se houver jogadores em outro ambiente, atribua email e senha a cada um antes
-- de exigir NOT NULL; nunca armazene senhas em texto puro.

ALTER TABLE jogadores
    MODIFY COLUMN email VARCHAR(150) NOT NULL,
    ADD COLUMN senha_hash VARCHAR(100) NOT NULL,
    ADD CONSTRAINT uk_jogadores_email UNIQUE (email);
