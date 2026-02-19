🏦 Vision Bank - Simulador Bancário Full-Stack
Acesse o Projeto Online

Java Spring Boot JavaScript Docker

O Vision Bank é uma aplicação Full-Stack que simula o ecossistema de um banco digital moderno. O objetivo principal deste projeto não é apenas realizar um simples CRUD, mas sim aplicar regras de negócio reais do mercado financeiro, garantindo transações seguras, histórico imutável e uma experiência de usuário (UX) fluida.

🚀 Principais Funcionalidades
O sistema foi desenvolvido com foco em segurança e usabilidade, contando com features avançadas:

Autenticação Segura: Login robusto utilizando Spring Security e tokens JWT.
Motor de PIX Dinâmico: * Geração de QR Codes (Base64) com trava de expiração (validade configurável, simulando cobranças de e-commerce).
Leitura de QR Codes utilizando a câmera do dispositivo (PC/Mobile) diretamente no navegador via JavaScript puro.
Suporte a pagamento via "Pix Copia e Cola".
Tela de validação de destinatário antes da efetivação da transação.
Transações Financeiras: Lógica ACID para garantir que transferências, saques e depósitos não gerem inconsistências de saldo.
Extrato Detalhado: Histórico completo de movimentações com identificação de entradas e saídas.
🛠️ Tecnologias Utilizadas
Backend & Infraestrutura
Java 17+
Spring Boot (Web, Data JPA, Security)
JWT (JSON Web Token) para Autenticação
Jackson (ObjectMapper) para manipulação de JSON
ZXing Library para geração de QR Code
Docker para containerização e padronização do ambiente
Deploy da API: Render
Frontend
HTML5 & CSS3 (Flexbox, Animações, Layout Responsivo, Modais)
JavaScript (Vanilla) (Consumo de APIs, manipulação de DOM, LocalStorage)
HTML5-QRCode (Biblioteca para acesso ao hardware da câmera)
Deploy do Front: Vercel
⚙️ Como executar o projeto localmente
Caso queira rodar a aplicação na sua máquina em vez de usar a versão online:

Pré-requisitos
Java JDK 17+
Docker e Docker Compose (Opcional, caso utilize container para o banco de dados)
Maven
Passos para rodar
Clone o repositório:
git clone [https://github.com/Raphaelgg99/visionbank.git](https://github.com/Raphaelgg99/visionbank.git)
