# 🏦 Vision Bank - Banco Digital Full Stack

![Badge em Desenvolvimento](http://img.shields.io/static/v1?label=STATUS&message=EM%20DESENVOLVIMENTO&color=GREEN&style=for-the-badge)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![HTML5](https://img.shields.io/badge/html5-%23E34F26.svg?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/css3-%231572B6.svg?style=for-the-badge&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=%23F7DF1E)

## 💻 Sobre o Projeto

O **Vision Bank** é uma aplicação Full Stack de simulação bancária. O objetivo foi criar um sistema robusto onde usuários podem abrir contas, realizar transações financeiras reais (simuladas) e visualizar seu histórico, com foco em segurança e boas práticas de desenvolvimento.

O sistema conta com um **Backend** poderoso em Java com Spring Boot rodando na nuvem (Render) e um **Frontend** responsivo em HTML/CSS/JS hospedado na Vercel.

---

## 🌐 Links do Projeto (Live Demo)

* ** Acesse o site: ** [https://visionbank-front.vercel.app](https://visionbank-front.vercel.app)

---

## ⚙️ Funcionalidades

### 🔐 Autenticação e Segurança
* [x] Criação de conta com validação de e-mail único.
* [x] Login seguro com geração de **Token JWT**.
* [x] Proteção de rotas (apenas usuários logados acessam o dashboard).
* [x] Criptografia de senhas com BCrypt.

### 💰 Operações Financeiras
* [x] **Visualizar Saldo:** Atualizado em tempo real.
* [x] **Depositar:** Adicionar valores à conta.
* [x] **Sacar:** Retirada com validação de saldo insuficiente.
* [x] **Transferência:** Envio de valores entre contas do Vision Bank.

### 📊 Histórico
* [x] **Extrato Bancário:** Listagem completa de todas as transações (entradas e saídas) com data e hora.

---
---

## 🛠 Tecnologias Utilizadas

### Backend (API REST)
* **Java 17**: Linguagem principal.
* **Spring Boot 3**: Framework para agilidade no desenvolvimento.
* **Spring Data JPA**: Para persistência de dados.
* **Spring Security + JWT**: Para autenticação e autorização stateless.
* **PostgreSQL**: Banco de dados relacional (Produção).
* **H2 Database**: Banco de dados em memória (Dev/Testes).
* **Maven**: Gerenciador de dependências.

### Frontend
* **HTML5 & CSS3**: Estrutura e estilização moderna.
* **Vanilla JavaScript (ES6+)**: Lógica do cliente, manipulação do DOM e consumo da API (Fetch API).
* **FontAwesome**: Ícones.

### Infraestrutura & Deploy
* **Render**: Hospedagem da API e do Banco de Dados PostgreSQL.
* **Vercel**: Hospedagem do Frontend estático.

---

## 🚀 Como rodar o projeto localmente

### Pré-requisitos
* Java 17 instalado.
* Maven instalado.
* Git instalado.

### 1. Backend
```bash
# Clone o repositório
git clone [https://github.com/Raphaelgg99/visionbank.git]

# Entre na pasta
cd visionbank-backend

# Instale as dependências e rode o projeto
mvn spring-boot:run
