# Collection Postman — Patota

Importe `patota-backend.postman_collection.json` no Postman. A collection usa
`baseUrl=http://127.0.0.1:8080` e autenticação HTTP Basic. O cadastro de
jogadores usa `noauth`.

Execute as pastas **01 a 04**, nesta ordem. A primeira requisição gera emails
de exemplo únicos; as respostas guardam os IDs nas variáveis da collection.
O fluxo cria uma patota, três mensalistas, um convidado e dois times, confirma
os participantes, balanceia os times e registra gols e assistências.

A pasta **05** contém operações alternativas e alterações que afetam o estado
do exemplo. Execute essas requisições individualmente. Recusar um participante
já confirmado ou um convidado já aprovado retorna conflito; para testar essas
alternativas, crie outra partida ou outro convidado pendente.

As senhas na collection são apenas valores de exemplo. Ajuste `baseUrl` e as
variáveis de credenciais se estiver usando outro ambiente.
