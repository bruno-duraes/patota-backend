-- Vínculos existentes passam a ser participantes; gerentes serão designados explicitamente.
ALTER TABLE patota_jogadores
    ADD COLUMN cargo ENUM('GERENTE', 'PARTICIPANTE') NOT NULL DEFAULT 'PARTICIPANTE';
