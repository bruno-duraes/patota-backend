# patota-backend

Backend Spring Boot para organizar jogadores, patotas e partidas semanais de futebol.

## Desenvolvimento

Requer Java 25, Maven e MySQL. A conexão padrão usa
`jdbc:mysql://localhost:3306/patota`.
Use `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` para configurar outra conexão.

Para subir o MySQL local com Docker Desktop:

```bash
docker compose -f compose.mysql.yml up -d
```

O banco `patota` fica disponível em `127.0.0.1:3306`. Os dados persistem no volume
`patota-backend_mysql-data`. Para parar o container sem apagar os dados, execute
`docker compose -f compose.mysql.yml down`.

Execute `mvn spring-boot:run` para iniciar e `mvn test` para testar.
O contexto e as regras pendentes estão em [docs/CONTEXTO_IMPLEMENTACAO.md](docs/CONTEXTO_IMPLEMENTACAO.md).

Se o Maven não estiver instalado no Windows, inicie a API junto com o MySQL via Docker:

```bash
docker compose -f compose.mysql.yml --profile api up -d
```

Para acompanhar a inicialização, use `docker compose -f compose.mysql.yml logs -f api`.

## API REST

A aplicação escuta em `127.0.0.1:8080` por padrão. O cadastro inicial de jogador
é público; as outras chamadas exigem HTTP Basic com email e senha. As senhas são
armazenadas como hash BCrypt. Use HTTPS antes de expor a API fora do computador;
`SERVER_ADDRESS` permite configurar outro endereço quando necessário.

| Operação | Endpoint |
| --- | --- |
| Criar/listar patotas | `POST` / `GET /api/patotas` |
| Buscar/atualizar patota | `GET` / `PUT /api/patotas/{id}` |
| Ativar/desativar patota | `PATCH /api/patotas/{id}/ativar` ou `/desativar` |
| Criar/listar jogadores | `POST` / `GET /api/jogadores` |
| Buscar/atualizar jogador | `GET` / `PUT /api/jogadores/{id}` |
| Ativar/desativar jogador | `PATCH /api/jogadores/{id}/ativar` ou `/desativar` |
| Adicionar/listar mensalistas | `POST` / `GET /api/patotas/{patotaId}/mensalistas` |
| Remover mensalista | `DELETE /api/patotas/{patotaId}/mensalistas/{jogadorId}` |
| Alterar cargo do vínculo | `PATCH /api/patotas/{patotaId}/mensalistas/{jogadorId}/cargo` |
| Criar/buscar partida | `POST /api/partidas`, `GET /api/partidas/{id}` |
| Listar partidas da patota | `GET /api/partidas?patotaId={id}` |
| Adicionar convidado pendente | `POST /api/partidas/{id}/convidados` |
| Consultar convidado | `GET /api/partidas/{id}/convidados/{convidadoId}` |
| Aprovar/recusar convidado | `PATCH /api/partidas/{id}/convidados/{convidadoId}/aprovar` ou `/recusar` |
| Listar participantes | `GET /api/partidas/{id}/participantes` |

Exemplos de corpos para `POST /api/jogadores`, `POST /api/patotas`,
`POST /api/patotas/1/mensalistas` e `POST /api/partidas`, nessa ordem:

```text
{"nome":"Ana","email":"ana@example.com","senha":"Senha1234"}
{"nome":"Amigos","descricao":"Jogo de quinta","local":"Quadra A"}
{"jogadorId":2}
{"patotaId":1,"dataHora":"2026-11-20T20:00:00","observacoes":"Levar coletes"}
```

O jogador autenticado que cria uma patota vira seu primeiro `GERENTE`. Novos
vínculos começam como `PARTICIPANTE`; somente gerentes ativos podem alterar o
cadastro da patota, seus vínculos e cargos, criar partidas e decidir sobre convidados.
Uma patota precisa manter pelo menos um gerente ativo.
Os endpoints administrativos usam `@RequerGerente` para consultar o cargo do
jogador autenticado na patota da operação. A anotação recebe uma expressão para
identificar a patota, como `@RequerGerente("#patotaId")`; em endpoints de partida,
a expressão pode obter a patota pelo ID da partida. Sem autenticação a API
retorna `401`; sem vínculo ativo de gerente na patota, `403`.

Criação retorna `201` e `Location`; erros de validação, ausência e conflito
retornam `400`, `404` e `409`.

Para adicionar um convidado que deverá ser aprovado, use
`POST /api/partidas/1/convidados` com `{"nome":"Carlos"}`. A API registra o
jogador autenticado como quem convidou.
O convidado entra com status `PENDENTE`. A listagem de participantes retorna
`mensalistas` e `convidados` separadamente. O convidador deve ser mensalista ativo
da patota; o convidado não precisa de cadastro em `jogadores`.

Para promover um participante, use `PATCH /api/patotas/1/mensalistas/2/cargo` com
`{"cargo":"GERENTE"}` e as credenciais de um gerente da patota.
