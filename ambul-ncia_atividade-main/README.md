# SOS Leitos - Roteamento Inteligente de Ambulâncias

Projeto interdisciplinar desenvolvido para a disciplina de Programação para Dispositivos Móveis, aplicando Teoria dos Grafos para resolver um problema de logística e saúde pública: o roteamento eficiente de ambulâncias em situações de emergência.

## O Problema
Quando uma ambulância está com um paciente na zona oeste de São Paulo, o tempo é crítico. O sistema precisa encontrar o hospital ou UPA mais próximo que tenha vagas disponíveis, evitando deslocamentos desnecessários para unidades superlotadas.

## Nossa Solução 
Para resolver isso de forma otimizada, modelamos a cidade como um Grafo Não Direcionado:
- Vértices (Nós): Os bairros.
- Arestas (Conexões): As vias que ligam os bairros adjacentes.
- Atributos: Cada bairro possui uma lista de hospitais, com suas respectivas capacidades totais e leitos ocupados.

O coração do aplicativo roda um algoritmo de Busca em Largura (BFS - Breadth-First Search). Escolhemos o BFS porque ele nos permite analisar as opções em "níveis de distância", garantindo que a ambulância procure as vagas progressivamente do bairro atual para os vizinhos mais distantes.

### Regras de Negócio:
1. Prioridade Local: Busca vaga primeiro no bairro onde a ambulância está. Se houver mais de um hospital com vaga, escolhe o que tem o maior número de leitos livres.
2. Expansão Adjacente: Se não achar, expande a busca para os vizinhos imediatos (Nível 1), seguindo a regra de priorizar quem tem mais vagas.
3. Limite de Deslocamento: O algoritmo trava a busca em um limite máximo de 3 bairros de distância.
4. Plano de Contingência: Se rodar os 3 níveis de distância e nenhum hospital tiver vaga livre, o algoritmo encaminha a ambulância para o hospital menos sobrecarregado (menor taxa de ocupação %) entre todos os analisados na região.

## Stack Tecnológica

* Linguagem: Java
* Plataforma: Android SDK
* Estrutura de Dados: Collections (HashMap, Queue, Set) para manipulação do grafo em memória.
* Persistência: Banco de Dados SQLite (Planejado)

## Membros da Equipe

* Emanuelly
* Gabriel Alex
* Gustavo Vinicius
* Kemilly
* Victor Neves

---
Projeto acadêmico - 2026.
