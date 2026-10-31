# patota-backend

Backend Spring Boot para organizar jogadores, patotas e partidas semanais de futebol.

## Desenvolvimento

Requer Java 25, Maven e MySQL. A conexão padrão continua em
`jdbc:mysql://localhost:3306/futbola` para preservar os dados existentes.
Use `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` para configurar outra conexão.

Execute `mvn spring-boot:run` para iniciar e `mvn test` para testar.
O contexto e as regras pendentes estão em [docs/CONTEXTO_IMPLEMENTACAO.md](docs/CONTEXTO_IMPLEMENTACAO.md).
