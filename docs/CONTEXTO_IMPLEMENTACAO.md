# Contexto de implementação — Patota

Atualizado em 15/11/2026.

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
PartidaEntity 1 ── N JogadorPartidaEntity N ── 1 JogadorEntity
PartidaEntity 1 ── N PartidaConvidadoEntity N ── 1 JogadorEntity (convidador)
PartidaEntity 1 ── N TimePartidaEntity
TimePartidaEntity 1 ── N JogadorPartidaEntity
TimePartidaEntity 1 ── N PartidaConvidadoEntity
```

- `PatotaJogadorEntity` representa o vínculo de um mensalista com uma patota.
  O cargo do vínculo é `PARTICIPANTE` por padrão ou `GERENTE`. O jogador que cria
  uma patota autenticado com email e senha torna-se o primeiro gerente.
- `JogadorPartidaEntity` representa um mensalista convocado para a partida. Nasce
  com status `PENDENTE` e referencia um jogador cadastrado, do qual lê nota e tipo.
- `PartidaConvidadoEntity` representa um convidado daquela partida, com nome e
  referência ao jogador que o convidou. Não cria `JogadorEntity` nem vínculo com
  a patota. Nasce com status `PENDENTE` e aguarda aprovação de um administrador.
  Guarda nota e tipo próprios.
- Nota varia de 1 a 5; tipo é `LINHA` ou `GOLEIRO`. O padrão é nota 3 e `LINHA`.
- A patota configura `maximoJogadoresPorPartida` (padrão 12, de 2 a 100).
  Cada partida copia o valor na criação. Apenas os confirmados contam no limite,
  somando mensalistas e convidados; respostas pendentes podem esperar uma vaga.
- Os status de participante são `PENDENTE`, `CONFIRMADO`, `RECUSADO` e `AUSENTE`.
- `TimePartidaEntity` guarda nome, cor e placar de um time criado para uma partida.
- Mensalistas e convidados implementam `ParticipanteDaPartida` e guardam seus
  próprios gols, assistências e vínculo opcional com um time. A tabela
  `escalacoes_partida` foi removida.
- Vitória, derrota e empate devem ser derivados do placar do time, sem serem gravados
  em cada jogador.

## Criação de partida

`PartidaService.criar(CriarPartidaDTO)`:

1. valida os dados obrigatórios;
2. busca a patota por `PatotaService.buscarPatota`;
3. verifica se a patota está ativa e possui local cadastrado;
4. cria a partida como `AGENDADA`, copiando o local da patota para preservar o histórico;
5. busca vínculos ativos em `PatotaJogadorService`;
6. cria um `JogadorPartidaEntity` pendente para cada vínculo por meio de
   `JogadorPartidaService`.

O método é `@Transactional`. Uma falha ao criar os registros deve fazer rollback
da partida e de todos os jogadores da partida.

## Regras pendentes de implementar

- Atualização de placar; futuramente, avaliar uma entidade de eventos da
  partida para registrar cada gol e assistência individualmente.
- Verificação de email, recuperação de senha e HTTPS antes de expor a API fora do
  ambiente local.
- Testes das demais regras de negócio.

## Cadastro implementado

`PatotaService` e `JogadorService` criam, atualizam e ativam/desativam registros.
`PatotaJogadorService` adiciona, remove e reativa mensalistas. O vínculo é único
por patota e jogador. Jogadores inativos não entram na convocação de novas partidas.
Essas operações são expostas em `/api/patotas`, `/api/jogadores` e
`/api/patotas/{patotaId}/mensalistas`. A criação e consulta de partidas usam
`/api/partidas`. Os controllers validam os corpos, retornam DTOs de resposta e
traduzem falhas de validação, ausência e conflito para `400`, `404` e `409`.
`POST /api/partidas/{id}/convidados` cadastra um convidado pendente, e
`GET /api/partidas/{id}/participantes` consulta as duas tabelas e devolve listas
separadas de mensalistas e convidados. O convidador é o jogador autenticado e
precisa ser mensalista ativo da patota da partida. Só gerentes ativos podem
aprovar ou recusar convidados; a decisão é registrada em `respondidaEm`.
Os endpoints administrativos usam `@RequerGerente`, apoiada por
`AutorizacaoGerente`, para conferir o cargo `GERENTE` no vínculo ativo do jogador
autenticado com a patota. A anotação recebe uma expressão SpEL que resolve o ID
da patota a partir do parâmetro do endpoint, do corpo ou da partida.
`MontagemTimesService` cria e lista times, atribui mensalistas e convidados
confirmados a um time da mesma partida, remove a atribuição e registra gols e
assistências em cada participante. Os endpoints de escrita exigem gerente da
patota; a consulta de times usa `GET /api/partidas/{id}/times`.
O próprio mensalista responde à convocação com `PATCH` em
`/api/partidas/{id}/mensalistas/{participanteId}/confirmar` ou `/recusar`.
`POST /api/partidas/{id}/times/balancear` exige um goleiro confirmado para cada
time, distribui jogadores de linha por quantidade e soma de notas e não altera
times após o registro de estatísticas.
Confirmação do mensalista e aprovação de convidado bloqueiam a linha da partida
antes de contar os confirmados, para impedir que duas respostas simultâneas
ultrapassem o limite.

## Banco local

O arquivo `compose.mysql.yml` deste repositório sobe o container `patota-mysql`
(MySQL 9), exposto apenas em `127.0.0.1:3306`, com o banco `patota` e o volume
persistente `patota-backend_mysql-data`.

Para subir:

```bash
docker compose -f compose.mysql.yml up -d
```

Sem Maven instalado no Windows, a API também pode subir pelo Compose com
`docker compose -f compose.mysql.yml --profile api up -d`. Ela fica acessível em
`127.0.0.1:8080` e usa o MySQL do mesmo projeto Compose.

`application.properties` aceita `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`; os padrões
locais são `jdbc:mysql://localhost:3306/patota`, `root` e `root`.

O banco padrão também se chama `patota`. O volume mantém os dados após o comando
`docker compose -f compose.mysql.yml down`; não use `down -v` se quiser preservá-los.
Em bancos que já tinham o esquema anterior, aplique a migração em
`docs/migracoes/2026-11-15-jogador-partida-e-convidado.sql` antes de iniciar a
nova versão da API. O `ddl-auto=update` cria a nova tabela, mas não remove colunas
antigas por conta própria.
O cargo do vínculo foi adicionado pela migração
`docs/migracoes/2026-11-15-cargo-patota.sql`; vínculos existentes passam a
`PARTICIPANTE`.
O cadastro com senha exige a migração
`docs/migracoes/2026-11-15-credenciais-jogador.sql`. Se já houver jogadores,
prepare credenciais individuais antes de exigir os novos campos obrigatórios.
Para migrar as escalações antigas sem perder time, gols e assistências, aplique
`docs/migracoes/2026-11-15-estatisticas-nos-participantes.sql` com a API parada.
Em seguida, aplique `docs/migracoes/2026-11-15-nota-e-tipo-jogador.sql` para
adicionar nota e tipo aos cadastros existentes.
Por fim, aplique `docs/migracoes/2026-11-15-limite-jogadores-partida.sql` para
configurar os limites nas patotas e copiar o valor para as partidas existentes.

## Verificação

Execute `mvn test` em um ambiente com Maven. Os testes usam H2 em memória e não
exigem MySQL.
