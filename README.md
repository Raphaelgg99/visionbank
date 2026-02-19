# 🏦 Vision Bank - Simulador Bancário Full-Stack
`Java` `Spring Boot` `PostgreSQL` `JavaScript` `Docker` `Swagger` `JUnit` 

🌐 **[Acesse o Projeto Online (Front-end)](https://visionbank-front.vercel.app/#)**

📖 **[Acesse a Documentação da API (Swagger)](https://visionbank-back.onrender.com/swagger-ui/index.html#/)**


O Vision Bank é uma aplicação Full-Stack que simula o ecossistema de um banco digital moderno. O objetivo principal deste projeto não é apenas realizar um simples CRUD, mas sim aplicar regras de negócio reais do mercado financeiro, garantindo transações seguras, histórico imutável, documentação clara e uma experiência de usuário (UX) fluida.

## 🚀 Principais Funcionalidades
O sistema foi desenvolvido com foco em segurança, usabilidade e boas práticas de engenharia de software, contando com features avançadas:

* **Autenticação Segura:** Login robusto utilizando Spring Security e tokens JWT.
* **Motor de PIX Dinâmico:** * Geração de QR Codes (Base64) com trava de expiração (validade configurável, simulando cobranças de e-commerce).
  * Leitura de QR Codes utilizando a câmera do dispositivo (PC/Mobile) diretamente no navegador via JavaScript puro.
  * Suporte a pagamento via "Pix Copia e Cola".
  * Tela de validação de destinatário antes da efetivação da transação.
* **Transações Financeiras Seguras:** Lógica ACID aplicada no banco de dados para garantir que transferências, saques e depósitos não gerem inconsistências de saldo sob nenhuma hipótese.
* **Extrato Detalhado:** Histórico completo de movimentações com identificação detalhada de entradas e saídas.
* **Documentação Interativa (API):** Interface gráfica gerada pelo Swagger (OpenAPI) listando e detalhando todos os endpoints, regras de negócio e retornos HTTP, permitindo testes práticos (inclusive rotas autenticadas) direto pelo navegador.

## 🛠️ Tecnologias Utilizadas

### Backend & Infraestrutura
* **Java 17+**
* **Spring Boot 3** (Web, Data JPA, Security)
* **PostgreSQL** (Banco de dados relacional robusto para integridade financeira)
* **Springdoc OpenAPI / Swagger UI** (Para documentação padronizada da API)
* **JUnit 5** (Para testes unitários e validação das regras de negócio)
* **JWT (JSON Web Token)** (Para controle de sessão e autorização)
* **Jackson (ObjectMapper)** (Para manipulação eficiente de dados JSON)
* **ZXing Library** (Para geração matemática dos QR Codes)
* **Docker & Docker Compose** (Para containerização e orquestração ágil do banco de dados e ambiente)
* **Deploy da API:** Render

### Frontend
* **HTML5 & CSS3** (Flexbox, Animações, Layout Responsivo, Modais)
* **JavaScript (Vanilla)** (Consumo de APIs, manipulação profunda do DOM, LocalStorage)
* **HTML5-QRCode** (Biblioteca para acesso ao hardware da câmera e escaneamento)
* **Deploy do Front:** Vercel

## ⚙️ Como executar o projeto localmente
Caso queira rodar a aplicação na sua máquina local de desenvolvimento:

### Pré-requisitos
* Java JDK 17+
* Maven
* Docker e Docker Compose (Para subir o contêiner do PostgreSQL de forma automatizada)

### Passos para rodar
1. Clone o repositório:
```bash
git clone [https://github.com/Raphaelgg99/visionbank.git](https://github.com/Raphaelgg99/visionbank.git)
```

2. Navegue até a pasta do backend e suba o banco de dados via Docker:
```bash
docker-compose up -d
```

3. Instale as dependências e inicie a aplicação Spring Boot usando a sua IDE (IntelliJ, Eclipse, VS Code) ou via terminal:
```bash
mvn spring-boot:run
```

4. **Explore a API:** Com o servidor rodando, acesse a documentação interativa do Swagger pelo navegador:
```text
http://localhost:8080/swagger-ui/index.html
```
