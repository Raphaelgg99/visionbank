document.addEventListener("DOMContentLoaded", function() {

    // --- 1. SELETORES (Pegando os elementos do HTML) ---
    // Usando os IDs e Classes que vi nas suas imagens
    var modal = document.getElementById("modal-login");
    var btnEntrar = document.querySelector(".botao-login"); // Botão do menu lá em cima
    var btnFechar = document.querySelector(".fechar-modal"); // O "X" da janela 
    var btnSimular = document.querySelector(".botao-simular")

    // Pegamos o formulário que está dentro do modal para ouvir o envio dele
    var formLogin = document.querySelector(".modal-login form");

    // --- 2. ABRE E FECHA A JANELA ---

    // Quando clicar em "Entrar", abre o modal
    if (btnEntrar) {
        btnEntrar.addEventListener("click", function(event) {
            event.preventDefault(); // Evita que o link recarregue a página
            modal.style.display = "flex"; // Mostra a janela centralizada
        });
    }

    // Quando clicar no "X", fecha
    if (btnFechar) {
        btnFechar.addEventListener("click", function() {
            modal.style.display = "none";
        });
    } 

    if (btnSimular){
        btnSimular.addEventListener("click", function(){ 
            event.preventDefault(); // Evita que o link recarregue a página
            modal.style.display = "flex";
        })
    }

    // Se clicar fora da caixinha branca (no fundo escuro), também fecha
    window.addEventListener("click", function(event) {
        if (event.target === modal) {
            modal.style.display = "none";
        }
    });

    // --- 3. LÓGICA DE LOGIN (Conectando com o Java) ---

    if (formLogin) {
        formLogin.addEventListener("submit", function(event) {
            // IMPEDE O RECARREGAMENTO DA PÁGINA
            event.preventDefault();

            // 1. Pega os valores dos inputs (IDs confirmados na imagem_2942d7.png)
            var emailDigitado = document.getElementById("email-login").value;
            var senhaDigitada = document.getElementById("senha-login").value;

            // 2. Monta o objeto que o Java espera receber
            var dadosLogin = {
                email: emailDigitado,
                senha: senhaDigitada
            };

            // 3. Envia para o Back-End
            fetch('https://visionbank-back.onrender.com/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(dadosLogin)
            })
            .then(response => {
                if (response.ok) {
                    return response.json(); // Se deu certo, pega o Token
                } else {
                    throw new Error('Falha na autenticação'); // Se deu erro (403 ou 401)
                }
            })
            .then(data => {
                console.log("Login com sucesso:", data);

                // --- SALVANDO DADOS NO NAVEGADOR ---

                // Salva o Token (obrigatório para as próximas telas)
                localStorage.setItem('token', data.token);

                // Salva o ID do usuário (se o seu Java estiver retornando o 'id')
                // Isso vai consertar aquele erro do dashboard não achar o saldo
                if (data.id) {
                    localStorage.setItem('usuarioId', data.id);
                }

                // Opcional: Salvar o nome para mostrar no "Olá, Nome"
                if (data.nome) {
                    localStorage.setItem('usuarioNome', data.nome);
                }

                alert("Bem-vindo ao Vision Bank!");
                window.location.href = "dashboard.html"; // Redireciona
            })
            .catch(error => {
                console.error('Erro:', error);
                alert("E-mail ou senha incorretos. Tente novamente.");
            });
        });
    }
});
