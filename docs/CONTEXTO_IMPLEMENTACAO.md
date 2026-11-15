# Contexto de implementação — Patota

Atualizado em 31/10/2026.

## Objetivo

Aplicação pessoal para organizar jogos semanais de futebol de uma patota:

- cadastrar jogadores e patotas;
- criar partidas semanais e convocar automaticamente os mensalistas;
- registrar confirmação de presença, convidados, times e estatísticas.

## Convenções do código

- Entidades terminam com `Entity`.
- Enums começam com `E`.
- Objetos de transporte terminam com `DTO`.
- Datas usam `LocalDateTime`; o sistema não trata fuso horário.
- Pacotes atuais: `model/entity`, `model/enums`, `model/dto`, `model/repository` e `model/service`.

## Modelo atual

```text
PatotaEntity 1 ── N PatotaJogadorEntity N ── 1 JogadorEntity
PatotaEntity 1 ── N PartidaEntity
PartidaEntity 1 ── N PresencaPartidaEntity N ── 1 JogadorEntity
PartidaEntity 1 ── N TimePartidaEntity 1 ── N EscalacaoPartidaEntity
PresencaPartidaEntity 1 ── 0..1 EscalacaoPartidaEntity
```

- `PatotaJogadorEntity` representa o vínculo de um mensalista com uma patota.
- `PresencaPartidaEntity` representa o convite/participação na partida e possui
  status (`PENDENTE`, `CONFIRMADA`, `RECUSADA`, `AUSENTE`) e tipo
  (`MENSALISTA`, `CONVIDADO`).
- `TimePartidaEntity` guarda nome, cor e placar de um time criado para uma partida.
- `EscalacaoPartidaEntity` associa uma presença a um time e armazena gols e assistências.
- Vitória, derrota e empate devem ser derivados do placar do time, sem serem gravados
  em cada jogador.

## Criação de partida

`PartidaService.criar(CriarPartidaDTO)`:

1. valida os dados obrigatórios;
2. busca a patota por `PatotaService.buscarPatota`;
3. verifica se a patota está ativa e possui local cadastrado;
4. cria a partida como `AGENDADA`, copiando o local da patota para preservar o histórico;
5. busca vínculos ativos em `PatotaJogadorService`;
6. cria uma presença pendente de tipo `MENSALISTA` para cada vínculo por meio de
   `PresencaPartidaService`.

O método é `@Transactional`. Uma falha ao criar convites deve fazer rollback da partida
e de todas as presenças.

## Regras pendentes de implementar

- Expor cadastro e manutenção de patotas, jogadores e mensalistas por controllers e DTOs de resposta.
- Confirmação/recusa de presença e inclusão de convidados.
- Criação de times e escalações: somente presenças confirmadas, pertencentes à mesma
  partida, podem ser escaladas uma única vez.
- Atualização de placar e estatísticas; futuramente, avaliar uma entidade de eventos da
  partida para registrar cada gol e assistência individualmente.
- DTOs de resposta, controllers, tratamento padronizado de exceções e testes das
  demais regras de negócio.

## Cadastro implementado

`PatotaService` e `JogadorService` criam, atualizam e ativam/desativam registros.
`PatotaJogadorService` adiciona, remove e reativa mensalistas. O vínculo é único
por patota e jogador. Jogadores inativos não entram na convocação de novas partidas.
Essas operações ainda não estão expostas por HTTP.

## Banco local

O arquivo `compose.mysql.yml` deste repositório sobe o container `patota-mysql`
(MySQL 9), exposto apenas em `127.0.0.1:3306`, com o banco `patota` e o volume
persistente `patota-backend_mysql-data`.

Para subir:

```bash
docker compose -f compose.mysql.yml up -d
```

`application.properties` aceita `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`; os padrões
locais são `jdbc:mysql://localhost:3306/patota`, `root` e `root`.

O banco padrão também se chama `patota`. O volume mantém os dados após o comando
`docker compose -f compose.mysql.yml down`; não use `down -v` se quiser preservá-los.

## Verificação

Execute `mvn test` em um ambiente com Maven. Os testes usam H2 em memória e não
exigem MySQL.
