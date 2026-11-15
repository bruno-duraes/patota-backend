-- Execute com a API parada. Antes, verifique se não há registros antigos do tipo CONVIDADO:
-- SELECT COUNT(*) FROM presencas_partida WHERE tipo_participacao = 'CONVIDADO';
-- Esses registros precisam de migração manual, pois o esquema antigo não guardava quem convidou.

RENAME TABLE presencas_partida TO jogador_partida;

ALTER TABLE jogador_partida
    DROP COLUMN tipo_participacao,
    MODIFY COLUMN status ENUM(
        'PENDENTE', 'CONFIRMADA', 'CONFIRMADO', 'RECUSADA', 'RECUSADO', 'AUSENTE'
    ) NOT NULL;

UPDATE jogador_partida SET status = 'CONFIRMADO' WHERE status = 'CONFIRMADA';
UPDATE jogador_partida SET status = 'RECUSADO' WHERE status = 'RECUSADA';

ALTER TABLE jogador_partida
    MODIFY COLUMN status ENUM('PENDENTE', 'CONFIRMADO', 'RECUSADO', 'AUSENTE') NOT NULL;

ALTER TABLE escalacoes_partida
    RENAME COLUMN presenca_partida_id TO jogador_partida_id;

-- A tabela partida_convidado será criada pelo Hibernate no próximo início da API.
