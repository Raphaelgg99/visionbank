document.getElementById("nextToEmail").addEventListener("click", function() {

    // 1. Pega o valor do nome
    var nomeInput = document.getElementById("nome").value;

    // 2. Verifica se está vazio
    if (nomeInput.trim() === "") {
        alert("Por favor, digite seu nome para continuar!");
        return; // Para tudo e não deixa avançar
    }

    // 3. Se tiver nome, aí sim avança
    document.getElementById("nomeField").classList.add("hidden");
    document.getElementById("emailField").classList.remove("hidden");
});

document.getElementById("nextToSenha").addEventListener("click", function() {

    // 1. Pega o valor do e-mail
    // (Corrigi o nome da variável para 'emailInput' para ficar organizado)
    var emailInput = document.getElementById("email").value;

    // 2. A "Fórmula Mágica" (Regex) para validar e-mail
    // Tradução: texto + @ + texto + . + texto
    var emailPadrao = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    // 3. Validação: Verifica se está vazio
    if (emailInput.trim() === "") {
        alert("Por favor, digite seu e-mail.");
        return;
    }

    // 4. Validação: Verifica se o formato é válido
    if (!emailPadrao.test(emailInput)) {
        alert("Por favor, digite um e-mail válido (ex: nome@exemplo.com)");
        return; // Para tudo e não deixa avançar
    }
    // 3. Se tiver nome, aí sim avança
    document.getElementById("emailField").classList.add("hidden");
    document.getElementById("senhaField").classList.remove("hidden");
});

document.getElementById("nextToRepetirSenha").addEventListener("click", function() {

    // 1. Pega o valor que está no campo senha
    var senhaInput = document.getElementById("senha").value;

    // --- INÍCIO DAS VALIDAÇÕES ---

    // Verifica se tem menos de 8 caracteres
    if (senhaInput.length < 8) {
        alert("A senha deve ter no mínimo 8 caracteres.");
        return;
    }

    // Verifica se tem Letra Maiúscula (A-Z)
    if (!/[A-Z]/.test(senhaInput)) {
        alert("A senha precisa ter pelo menos uma letra Maiúscula.");
        return;
    }

    // Verifica se tem Letra Minúscula (a-z)
    if (!/[a-z]/.test(senhaInput)) {
        alert("A senha precisa ter pelo menos uma letra minúscula.");
        return;
    }

    // Verifica se tem Números (0-9)
    if (!/[0-9]/.test(senhaInput)) {
        alert("A senha precisa ter pelo menos um número.");
        return;
    }

    // Verifica se tem Caractere Especial (!@#$% etc)
    // Essa lista dentro dos colchetes são os caracteres permitidos
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(senhaInput)) {
        alert("A senha precisa ter pelo menos um caractere especial (ex: @, #, $).");
        return;
    }

    // --- FIM DAS VALIDAÇÕES ---

    // 3. Se tiver nome, aí sim avança
    document.getElementById("senhaField").classList.add("hidden");
    document.getElementById("repetirSenhaField").classList.remove("hidden");
});

document.getElementById("nextToSaldo").addEventListener("click", function() {
    // 1. Pega o valor da primeira senha (que ele digitou antes)
    var senhaOriginal = document.getElementById("senha").value;

    // 2. Pega o valor da senha repetida (que ele acabou de digitar)
    var senhaRepetida = document.getElementById("repetirSenha").value;

    // --- VALIDAÇÕES ---

    // Verifica se o campo está vazio
    if (senhaRepetida.trim() === "") {
        alert("Por favor, confirme sua senha.");
        return;
    }

    // A MÁGICA: Verifica se as duas são IGUAIS
    if (senhaOriginal !== senhaRepetida) {
        alert("As senhas não coincidem! Por favor, tente novamente.");

        // Dica de UX: Limpa o campo de repetir para ele digitar de novo
        document.getElementById("repetirSenha").value = "";
        document.getElementById("repetirSenha").focus(); // Joga o cursor lá dentro
        return; // Para tudo e não deixa avançar
    }

    // --- SUCESSO ---

    // Se chegou até aqui, as senhas são iguais. Avança!
    document.getElementById("repetirSenhaField").classList.add("hidden");
    document.getElementById("saldoField").classList.remove("hidden");
});

document.getElementById("finalizarCadastro").addEventListener("click", function(event) {
    // 1. Impede o recarregamento da página
    event.preventDefault();

    // 2. Captura TODOS os dados do formulário novamente
    // (Precisamos pegar nome, email e senha de novo porque as variáveis dos passos anteriores eram locais)
    var nome = document.getElementById("nome").value;
    var email = document.getElementById("email").value;
    var senha = document.getElementById("senha").value;
    var saldoInput = document.getElementById("saldo").value;
    var saldo = parseFloat(saldoInput);

    // --- VALIDAÇÕES DO SALDO (Mantendo as suas) ---
    if (saldoInput.trim() === "" || isNaN(saldo)) {
        alert("Por favor, digite um valor numérico válido.");
        return;
    }
    if (saldo < 0) {
        alert("O saldo não pode ser negativo.");
        return;
    }
    if (saldo > 1000000) {
        alert("O saldo inicial máximo permitido é de 1.000.000.");
        return;
    }

    // 3. Monta o Objeto para CRIAR A CONTA
    var dadosUsuario = {
        nome: nome,
        email: email,
        senha: senha,
        saldo: saldo
    };

    // --- INÍCIO DO PROCESSO (Criação -> Login -> Dashboard) ---

    // PASSO A: Tenta Criar a Conta
    fetch('http://localhost:8080/conta/adicionar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dadosUsuario)
    })
    .then(response => {
        if (response.ok) {
            return response.json(); // Conta criada com sucesso!
        } else {
            throw new Error('Erro ao criar conta (Verifique se o email já existe)');
        }
    })
    .then(contaCriada => {
        // PASSO B: Conta criada! Agora vamos fazer o LOGIN AUTOMÁTICO
        console.log("Conta criada! Iniciando login automático...");

        var dadosLogin = {
            email: email, // Usa o mesmo email do cadastro
            senha: senha  // Usa a mesma senha do cadastro
        };

        return fetch('http://localhost:8080/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dadosLogin)
        });
    })
    .then(responseLogin => {
        if (responseLogin.ok) {
            return responseLogin.json(); // Pega o Token
        } else {
            throw new Error('Conta criada, mas falha ao realizar login automático.');
        }
    })
    .then(dataLogin => {
        // PASSO C: Login realizado! Salva os dados e redireciona

        // 1. Salva o Token (Fundamental para o Dashboard)
        localStorage.setItem('token', dataLogin.token);

        // 2. Salva o ID do usuário (Fundamental para carregar o saldo correto)
        if (dataLogin.id) {
            localStorage.setItem('usuarioId', dataLogin.id);
        }

        // 3. Opcional: Salvar nome para a saudação ficar rápida
        localStorage.setItem('usuarioNome', nome);

        alert("Conta criada com sucesso! Entrando...");
        window.location.href = "dashboard.html"; // Vai direto pro sistema!
    })
    .catch(error => {
        console.error('Erro:', error);
        alert("Ocorreu um erro: " + error.message);
    });
});
