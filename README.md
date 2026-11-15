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
