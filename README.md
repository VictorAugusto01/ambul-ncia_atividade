# 🚑 Sistema de Triagem e Rota de Ambulâncias

Aplicativo Android desenvolvido para auxiliar na triagem médica de pacientes e traçar a rota mais eficiente para a ambulância até o hospital recomendado, utilizando os serviços de mapeamento e roteamento do Google.

## 📱 Funcionalidades
* **Triagem de Pacientes:** Cadastro e avaliação inicial do estado do paciente.
* **Visualização no Mapa:** Interface interativa exibindo a localização da ambulância e dos hospitais próximos.
* **Roteamento Inteligente:** Traçado de rota em tempo real usando a Directions API do Google.

## 🛠️ Tecnologias Utilizadas
* **Linguagem:** Java / Kotlin
* **Plataforma:** Android SDK (mínimo SDK 26)
* **Ambiente:** Android Studio
* **APIs Integradas:**
    * Maps SDK for Android
    * Directions API

---

## 🚀 Como rodar o projeto localmente

Como este projeto utiliza a API do Google Maps para traçar rotas e exibir localizações, você precisará configurar uma chave de API própria para que o mapa funcione corretamente. O repositório foi configurado usando as melhores práticas para manter as credenciais seguras.

**1. Clone o repositório:**
```bash
git clone [https://github.com/VictorAugusto01/ambul-ncia_atividade.git](https://github.com/VictorAugusto01/ambul-ncia_atividade.git)
```

**2. Obtenha a chave da API do Google:**
* Acesse o [Google Cloud Console](https://console.cloud.google.com/).
* Crie um projeto e certifique-se de ativar as seguintes APIs: **Maps SDK for Android** e **Directions API**.
* Gere uma credencial (API Key).

**3. Configure a chave no projeto:**
* Abra o projeto clonado no Android Studio.
* Na raiz do projeto, procure pelo arquivo `local.properties` (dentro da visão *Project* ou *Gradle Scripts*). Se ele não existir, você pode criá-lo.
* Adicione a seguinte linha na parte inferior do arquivo, colando a sua chave sem aspas:
```properties
MAPS_API_KEY=sua_chave_de_api_gerada_aqui
```

**4. Sincronize e Execute:**
* Clique no botão **"Sync Project with Gradle Files"** (ícone de elefante com setinha) na barra superior do Android Studio.
* Dê o Play (Run) para testar no emulador ou no seu dispositivo físico.

---

## 👥 Squad de Desenvolvimento
Projeto desenvolvido em equipe para fins acadêmicos e construção de portfólio.

* **Emanuelly**
* **Gabriel Alex**
* **Gustavo Vinicius**
* **Kemilly**
* **Victor Neves**