# 🚑 SOS Leitos - Roteamento Inteligente de Ambulâncias

Aplicativo Android desenvolvido para otimizar o fluxo de emergências médicas, auxiliando na triagem de pacientes e utilizando Teoria dos Grafos para traçar a rota mais rápida até o hospital com leitos disponíveis na região.

## 📱 Funcionalidades
* **Dashboard em Tempo Real:** Painel com estatísticas de ambulâncias ativas, emergências do dia e tempo médio de resposta.
* **Triagem de Pacientes:** Busca de prontuários via RG com alertas visuais críticos (ex: risco de choque anafilático por alergias cadastradas).
* **Busca Inteligente de Vagas:** Utiliza o algoritmo de Busca em Largura (BFS) sobre um grafo de bairros para localizar o hospital mais próximo que possua leitos de UTI disponíveis no banco de dados.
* **Integração de Rota Zero-Cost:** Exibição do panorama no Maps SDK e transição fluida para o aplicativo nativo do Google Maps/Waze via URL Schemes (Intents) para navegação GPS *turn-by-turn*, dispensando o uso oneroso da Directions API.

## 🛠️ Tecnologias e Conceitos Utilizados
* **Linguagem:** Java (Android SDK 26+)
* **Arquitetura:** Single Activity com Múltiplos Fragments (Bottom Navigation)
* **Banco de Dados:** SQLite (Armazenamento local de usuários, pacientes, histórico e matriz de adjacência dos hospitais)
* **Algoritmos:** Teoria dos Grafos (BFS - Breadth-First Search)
* **APIs Integradas:**
  * Maps SDK for Android (Apenas para renderização do mapa base e marcadores)
  * Google Maps URL Schemes / Android Intents (Navegação GPS)

---

## 🚀 Como rodar o projeto localmente

Como o projeto utiliza o Maps SDK para renderizar o mapa visual dentro do aplicativo, é necessário configurar uma chave de API básica do Google Cloud (apenas para exibição, sem necessidade de ativar roteamento pago).

**1. Clone o repositório:**
> git clone https://github.com/VictorAugusto01/ambul-ncia_atividade.git

**2. Obtenha a chave da API do Google:**
* Acesse o [Google Cloud Console](https://console.cloud.google.com/).
* Crie um projeto e ative a **Maps SDK for Android**. *(Nota: A Directions API não é necessária para este projeto)*.
* Gere uma credencial (API Key).

**3. Configure a chave no projeto:**
* Abra o projeto clonado no Android Studio.
* Na raiz do projeto, procure pelo arquivo `local.properties` (dentro da visão *Project* ou *Gradle Scripts*). Se ele não existir, crie-o.
* Adicione a seguinte linha na parte inferior do arquivo, colando a sua chave sem aspas:
> MAPS_API_KEY=sua_chave_de_api_gerada_aqui

**4. Sincronize e Execute:**
* Clique no botão **"Sync Project with Gradle Files"** na barra superior do Android Studio.
* Dê o Play (Run) para testar no emulador ou no seu dispositivo físico. *(Recomenda-se testar a funcionalidade de Rota em um dispositivo físico com o Google Maps instalado).*

---

## 👥 Squad de Desenvolvimento
Projeto desenvolvido em equipe como Trabalho de Conclusão de Curso (TCC) para aplicação prática de Desenvolvimento Mobile e Estrutura de Dados.

* **Emanuelly**
* **Gabriel Alex**
* **Gustavo Vinicius**
* **Kemilly**
* **Victor Neves**