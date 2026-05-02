# 🚑 SOS Leitos - Roteamento Inteligente de Ambulâncias (Zona Oeste - SP)

Projeto interdisciplinar focado em logística hospitalar de emergência, utilizando **Teoria dos Grafos** para otimizar o encaminhamento de pacientes na Zona Oeste de São Paulo. Desenvolvido para a disciplina de Programação para Dispositivos Móveis.

## 📌 O Desafio Técnico
Em situações de emergência, o tempo de resposta é o fator determinante entre a vida e a morte. O problema central que resolvemos foi: **Como garantir que uma ambulância não perca tempo indo para um hospital que já está com o pronto-socorro lotado?**

## 🧠 Lógica e Algoritmos

### Modelagem em Grafos
Modelamos os bairros da Zona Oeste como um **Grafo Não-Direcionado**:
*   **Nós (Vértices):** Bairros (Lapa, Butantã, Pinheiros, Itaim Bibi, etc.).
*   **Conexões (Arestas):** As principais vias de acesso e ruas que ligam os bairros adjacentes.
*   **Atributos Dinâmicos:** Cada nó carrega dados em tempo real (via SQLite local) sobre a capacidade de leitos, vagas ocupadas e alertas médicos.

### Motor de Busca: BFS (Breadth-First Search)
Implementamos uma adaptação do algoritmo de **Busca em Largura**. A escolha do BFS foi estratégica para a saúde pública: ele nos permite varrer a cidade em "camadas" de distância, garantindo sempre a menor distância em termos de bairros percorridos.

1.  **Nível 0:** O algoritmo verifica primeiro os hospitais no próprio bairro onde a ambulância está.
2.  **Expansão:** Se todas as unidades locais estiverem lotadas, o motor expande para os vizinhos imediatos (Nível 1) e assim por diante.
3.  **Contingência:** Caso o sistema atinja o limite de 3 bairros (Nível 3) sem encontrar leitos livres, o algoritmo entra em modo de segurança e seleciona a unidade com a **menor taxa de ocupação percentual** da região, evitando o colapso do atendimento.

## 🚀 Funcionalidades A+
*   **Integração Real com GPS:** Uso do `FusedLocationProviderClient` para capturar a localização exata do aparelho e traçar a origem da rota dinamicamente no Google Maps.
*   **Terminal de Log Estético:** Exibição do "passo a passo" do algoritmo BFS na tela, formatado em estilo prompt de comando, permitindo que o socorrista entenda a decisão do sistema.
*   **Triagem e Prontuário:** Sistema de busca por RG que recupera dados críticos do paciente (alergias graves e tipo sanguíneo) diretamente do banco de dados.
*   **Histórico de Atendimentos:** Registro persistente de encaminhamentos anteriores para cada paciente.
*   **Persistência Local:** Banco de Dados SQLite estruturado com relacionamentos entre usuários, pacientes e a topologia do grafo.

## 🛠 Stack Tecnológica
*   **Linguagem:** Java (Android SDK)
*   **Mapa:** Google Maps Platform API
*   **Localização:** Google Play Services Location
*   **Database:** SQLite Puro (`SQLiteOpenHelper`)
*   **UI/UX:** XML Layouts com foco em alto contraste para uso em situações de emergência.

## 👥 Squad de Desenvolvimento
*   Emanuelly
*   Gabriel Alex
*   Gustavo Vinicius
*   Kemilly
*   Victor Neves

---
*Maio de 2026 - Projeto Acadêmico para PDM*
