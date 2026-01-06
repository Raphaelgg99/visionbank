document.addEventListener("DOMContentLoaded", function() {
    // 1. Recupera ID e Token
    const numeroDaConta = localStorage.getItem('usuarioId');
    const token = localStorage.getItem('token');

    // Se não tiver dados, chuta pro login
    if (!numeroDaConta || !token) {
        alert("Você precisa estar logado para acessar essa página!");
        window.location.href = "paginainicial.html";
        return;
    }

    // --- CORREÇÃO 1: CHAMAR AS DUAS FUNÇÕES ---
    carregarDadosDoDashboard(numeroDaConta, token); // Carrega Saldo e Nome
    buscarExtrato(numeroDaConta, token);            // Carrega a Tabela (Faltava essa linha)

    // Botão Sair
    const btnSair = document.getElementById("sair");
    if(btnSair) {
        btnSair.addEventListener("click", function() {
            localStorage.clear();
            window.location.href = "paginainicial.html";
        });
    }
});

function carregarDadosDoDashboard(numeroDaConta, token) {
    const tokenFormatado = token.startsWith("Bearer ") ? token : `Bearer ${token}`;

    fetch(`http://localhost:8080/conta/${numeroDaConta}`, {
        method: 'GET',
        headers: {
            'Authorization': tokenFormatado,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.status === 403 || response.status === 401) throw new Error("Sessão expirada");
        if (!response.ok) throw new Error("Erro ao buscar dados da conta");
        return response.json();
    })
    .then(dados => {
        // Atualiza Nome
        const elementoNome = document.getElementById("saldacao");
        if (elementoNome) {
            const primeiroNome = dados.nome.split(' ')[0];
            elementoNome.innerText = `Olá, ${primeiroNome}`;
        }

        // Atualiza Saldo
        const elementoSaldo = document.getElementById("valor-saldo");
        if (elementoSaldo) {
            const saldoFormatado = dados.saldo.toLocaleString('pt-BR', {
                minimumFractionDigits: 2, maximumFractionDigits: 2
            });
            elementoSaldo.innerText = saldoFormatado;

            if (dados.saldo < 0) {
                elementoSaldo.style.color = "#ff4d4d";
            } else {
                elementoSaldo.style.color = "#fff";
            }
        }
    })
    .catch(error => {
        console.error("Erro:", error);
        if (error.message === "Sessão expirada") {
            alert("Sua sessão expirou.");
            localStorage.clear();
            window.location.href = "paginainicial.html";
        }
    });
}

// Variável global para armazenar as transações trazidas do back-end
let todasTransacoesCache = [];

function buscarExtrato(numeroDaConta, token) {
    const tokenFormatado = token.startsWith("Bearer ") ? token : `Bearer ${token}`;

    fetch(`http://localhost:8080/conta/${numeroDaConta}/extrato`, {
        method: 'GET',
        headers: {
            'Authorization': tokenFormatado
        }
    })
    .then(r => r.json())
    .then(listaTransacoes => {
        // --- MUDANÇA AQUI ---
        // Salvamos a lista completa nesta variável global para usar no modal depois
        todasTransacoesCache = listaTransacoes;

        // Continua chamando a função do dashboard (miniatura) como antes
        atualizarTabelaTransacoes(listaTransacoes);
    })
    .catch(e => console.error("Erro ao carregar extrato:", e));
}

function atualizarTabelaTransacoes(listaTransacoes) {

    // 1. CAPTURA DOS ELEMENTOS HTML
    // Pegamos as referências da div que segura a tabela, da mensagem de "vazio" e do corpo da tabela (tbody)
    const tabelaContainer = document.querySelector('.tabela-container');
    const mensagemVazio = document.getElementById('mensagem-vazio');
    const tbody = document.querySelector('tbody');

    // SEGURANÇA: Se por algum motivo o HTML não tiver um 'tbody', a função para aqui para não dar erro.
    if(!tbody) return;

    // 2. LIMPEZA
    // Limpa todas as linhas antigas da tabela antes de desenhar as novas.
    // Se não fizer isso, a tabela vai duplicar as linhas toda vez que atualizar.
    tbody.innerHTML = '';

    // 3. VERIFICAÇÃO DE LISTA VAZIA
    // Se a lista for nula ou tiver tamanho 0 (nenhuma transação)
    if (!listaTransacoes || listaTransacoes.length === 0) {

        // Esconde a tabela
        if (tabelaContainer) tabelaContainer.style.display = 'none';
        // Mostra a mensagem "Nenhuma transação encontrada"
        if (mensagemVazio) mensagemVazio.style.display = 'block';
        return; // Sai da função
    }

    // Se tiver dados, faz o contrário: Mostra a tabela e esconde a mensagem.
    if (tabelaContainer) tabelaContainer.style.display = 'block';
    if (mensagemVazio) mensagemVazio.style.display = 'none';

    // 4. ORDENAÇÃO E FILTRO
    // .slice(-5): Pega apenas os últimos 5 itens da lista original
    // .reverse(): Inverte a ordem para que o mais recente fique no topo (índice 0)
    const ultimasTransacoes = listaTransacoes.slice(0,5);

    // 5. RECUPERAÇÃO DO ID DO USUÁRIO
    // Pegamos o ID de quem está logado no navegador agora.
    // Isso é crucial para saber se foi EU que mandei o dinheiro ou se EU recebi.
    const meuId = localStorage.getItem('usuarioId');

    // 6. LOOP (Gera uma linha para cada transação)
    ultimasTransacoes.forEach(transacao => {

        // --- FORMATAÇÃO DE DATA ---
        let dataFormatada = "Data Inválida";
        if(transacao.data) {
            // Tenta converter caso venha como String ISO ("2025-01-02")
            const dataObj = new Date(transacao.data);

            // Verifica se é uma data válida
            if(!isNaN(dataObj)) {
                dataFormatada = dataObj.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
            }
            // Tratamento especial caso o Java mande como Array [2025, 01, 02]
            else if (Array.isArray(transacao.data)) {
                const [ano, mes, dia] = transacao.data;
                // padStart(2, '0') garante que dia 5 vire "05"
                dataFormatada = `${String(dia).padStart(2, '0')}/${String(mes).padStart(2, '0')}`;
            }
        }

        // --- FORMATAÇÃO DE MOEDA ---
        // Converte o número (ex: 50.5) para formato Real (R$ 50,50)
        const valorFormatado = transacao.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

        // --- LÓGICA DE COR (O Pulo do Gato) ---

        // Passo A: Assume por padrão que é uma ENTRADA (Verde / Recebido)
        let classeCor = 'entrada';

        // Passo B: Verifica se fui EU que enviei essa transferência.
        // Checa se existe remetente E se o ID do remetente é igual ao MEU ID salvo no localStorage.
        const souEuRemetente = transacao.contaRemetente &&
                               String(transacao.contaRemetente.numeroDaConta) === String(meuId);

        // Passo C: Decide se deve mudar a cor para SAÍDA (Vermelho)
        // É saída se:
        // 1. O tipo for 'SAQUE'
        // 2. OU.. O tipo for 'TRANSFERENCIA' E eu for o remetente (souEuRemetente for true)
        // 3. OU.. O valor for negativo (garantia extra)
        if (transacao.tipo === 'SAQUE' ||
           (transacao.tipo === 'TRANSFERENCIA' && souEuRemetente) ||
           transacao.valor < 0) {

            classeCor = 'saida'; // Muda a classe CSS para ficar vermelho
        }

        // 7. CRIAÇÃO DO HTML
        const tr = document.createElement('tr');

        // Monta as células da tabela injetando as variáveis formatadas
        tr.innerHTML = `
            <td>${dataFormatada}</td>
            <td>${transacao.descricao || transacao.tipo || 'Movimentação'}</td>
            <td class="${classeCor}">${valorFormatado}</td>
        `;

        // Adiciona a linha criada dentro do corpo da tabela na tela
        tbody.appendChild(tr);
    });
}
// --- LÓGICA DO SAQUE ---
const modalSaque = document.getElementById("modal-saque");
const btnAbrirSaque = document.getElementById("saque");
const btnFecharSaque = document.getElementById("fechar-saque");
const btnConfirmarSaque = document.getElementById("btn-confirmar-saque");

if (btnAbrirSaque) {
    btnAbrirSaque.addEventListener("click", function(e) {
        e.preventDefault();
        modalSaque.style.display = "flex";
        document.getElementById("valorSaqueInput").value = "";
        document.getElementById("valorSaqueInput").focus();
    });
}

if (btnFecharSaque) {
    btnFecharSaque.addEventListener("click", function() {
        modalSaque.style.display = "none";
    });
}
window.addEventListener("click", function(e) {
    if (e.target === modalSaque) modalSaque.style.display = "none";
});

if (btnConfirmarSaque) {
    btnConfirmarSaque.addEventListener("click", function() {
        const valorInput = document.getElementById("valorSaqueInput").value;
        const valor = parseFloat(valorInput);

        if (!valor || valor <= 0) {
            alert("Valor inválido.");
            return;
        }

        const numeroDaConta = localStorage.getItem('usuarioId');
        const token = localStorage.getItem('token');
        const tokenFormatado = token.startsWith("Bearer ") ? token : `Bearer ${token}`;

        fetch(`http://localhost:8080/conta/${numeroDaConta}/sacar`, {
            method: 'PUT',
            headers: {
                'Authorization': tokenFormatado,
                'Content-Type': 'application/json'
            },
            body: valor
        })
        .then(response => {
            if (response.ok) return response.json();
            return response.text().then(text => { throw new Error(text || "Erro no saque"); });
        })
        .then(() => { // Removido argumento 'dadosAtualizados' se não for usar
            alert("Saque realizado!");
            modalSaque.style.display = "none";

            // --- CORREÇÃO 3: ATUALIZA TUDO APÓS SAQUE ---
            carregarDadosDoDashboard(numeroDaConta, token); // Atualiza Saldo
            buscarExtrato(numeroDaConta, token);            // Atualiza Tabela (mostra o novo saque)
        })
        .catch(error => {
            console.error("Erro no saque:", error);
            alert("Não foi possível sacar: " + error.message);
        });
    });
}

// ===============================
// --- LÓGICA DO DEPÓSITO ---
// ===============================

// Captura o elemento do modal de depósito pelo ID.
// Esse modal é a "janela" que aparece na tela para o usuário informar o valor.
const modalDeposito = document.getElementById("modal-deposito");

// Captura o botão que ABRE o modal de depósito
const btnAbrirDeposito = document.getElementById("deposito");

// Captura o botão que FECHA o modal de depósito
const btnFecharDeposito = document.getElementById("fechar-deposito");

// Captura o botão que CONFIRMA o depósito
const btnConfirmarDeposito = document.getElementById("btn-confirmar-deposito");


// =====================================
// ABRIR O MODAL DE DEPÓSITO
// =====================================

// Verifica se o botão existe na página antes de adicionar o evento
// Isso evita erro caso o script rode em uma página que não tenha esse botão
if (btnAbrirDeposito) {

    // Adiciona um listener para o clique no botão de depósito
    btnAbrirDeposito.addEventListener("click", function(e) {

        // Impede o comportamento padrão do botão (ex: submit de formulário)
        e.preventDefault();

        // Exibe o modal, usando display flex (geralmente usado para centralizar)
        modalDeposito.style.display = "flex";

        // Limpa o campo de valor do depósito
        document.getElementById("valorDepositoInput").value = "";

        // Coloca o cursor automaticamente dentro do input
        document.getElementById("valorDepositoInput").focus();
    });
}


// =====================================
// FECHAR O MODAL PELO BOTÃO "X"
// =====================================

// Verifica se o botão de fechar existe
if (btnFecharDeposito) {

    // Adiciona evento de clique no botão de fechar
    btnFecharDeposito.addEventListener("click", function() {

        // Oculta o modal
        modalDeposito.style.display = "none";
    });
}


// =====================================
// FECHAR O MODAL CLICANDO FORA DELE
// =====================================

// Adiciona um listener global para cliques na janela
window.addEventListener("click", function(e) {

    // Se o clique foi exatamente no fundo do modal (fora do conteúdo)
    if (e.target === modalDeposito) {

        // Fecha o modal
        modalDeposito.style.display = "none";
    }
});


// =====================================
// CONFIRMAR O DEPÓSITO
// =====================================

// Verifica se o botão de confirmar existe
if (btnConfirmarDeposito) {

    // Adiciona o evento de clique no botão de confirmar
    btnConfirmarDeposito.addEventListener("click", function() {

        // Obtém o valor digitado no input como string
        const valorInput = document.getElementById("valorDepositoInput").value;

        // Converte o valor para número decimal
        const valor = parseFloat(valorInput);

        // Validação do valor:
        // - Se não existir
        // - Se for zero
        // - Se for negativo
        if (!valor || valor <= 0) {
            alert("Valor inválido.");
            return; // Interrompe a execução
        }

        // Recupera o número da conta do usuário armazenado no localStorage
        const numeroDaConta = localStorage.getItem('usuarioId');

        // Recupera o token de autenticação
        const token = localStorage.getItem('token');

        // Garante que o token esteja no formato "Bearer ..."
        const tokenFormatado = token.startsWith("Bearer ")
            ? token
            : `Bearer ${token}`;

        // =====================================
        // REQUISIÇÃO PARA A API (DEPÓSITO)
        // =====================================

        fetch(`http://localhost:8080/conta/${numeroDaConta}/depositar`, {
            method: 'PUT', // Método HTTP usado para atualização

            headers: {
                'Authorization': tokenFormatado, // Token JWT
                'Content-Type': 'application/json' // Tipo do corpo da requisição
            },

            // Envia o valor do depósito no corpo da requisição
            body: valor
        })

        // Trata a resposta da API
        .then(response => {

            // Se a resposta for OK (status 200-299)
            if (response.ok) {
                return response.json();
            }

            // Caso contrário, lê o texto do erro e lança uma exceção
            return response.text().then(text => {
                throw new Error(text || "Erro no depósito");
            });
        })

        // Executa se o depósito for realizado com sucesso
        .then(() => {

            // Exibe mensagem de sucesso
            alert("Depósito realizado!");

            // Fecha o modal (OBS: aqui parece haver um erro de nome)
            modalDeposito.style.display = "none";

            // =====================================
            // ATUALIZAÇÃO DOS DADOS DO DASHBOARD
            // =====================================

            // Atualiza o saldo exibido
            carregarDadosDoDashboard(numeroDaConta, token);

            // Atualiza o extrato/tabela de movimentações
            buscarExtrato(numeroDaConta, token);
        })

        // Captura qualquer erro ocorrido no processo
        .catch(error => {

            // Exibe o erro no console (útil para debug)
            console.error("Erro no saque:", error);

            // Exibe mensagem de erro para o usuário
            alert("Não foi possível sacar: " + error.message);
        });
    });
}

// ===============================
// --- LÓGICA De TRANSFERENCIA ---
// ===============================

// Obtém o elemento do modal de transferência pelo ID
// Esse modal é a janela que aparece na tela para realizar a transferência
const modalTransferencia = document.getElementById("modal-transferencia");

// Obtém o botão que abre o modal de transferência
const btnAbrirTransferencia = document.getElementById("transferencia");

// Obtém o botão que fecha o modal (normalmente o "X" ou botão cancelar)
const btnFecharTransferencia = document.getElementById("fechar-transferencia");

// Obtém o botão que confirma a transferência
const btnConfirmarTransferencia = document.getElementById("btn-confirmar-transferencia");


// ========================
// 1. ABRIR O MODAL
// ========================

// Verifica se o botão de abrir o modal existe na página
// Isso evita erro caso o script seja carregado em uma página
// que não possui esse botão
if (btnAbrirTransferencia) {

    // Adiciona um evento de clique ao botão
    btnAbrirTransferencia.addEventListener("click", function(e) {

        // Impede o comportamento padrão do botão ou link
        // Exemplo: evita recarregar a página se for um <a>
        e.preventDefault();

        // Exibe o modal alterando o display para "flex"
        // Geralmente usado quando o modal é centralizado com Flexbox
        modalTransferencia.style.display = "flex";

        // Limpa o campo de valor da transferência
        document.getElementById("valorTransferenciaInput").value = "";

        // Limpa o campo do número da conta de destino
        document.getElementById("numeroContaInput").value = "";

        // Coloca o cursor automaticamente no campo de valor
        // para facilitar a digitação do usuário
        document.getElementById("numeroContaInput").focus();
    });
}


// ========================
// 2. FECHAR O MODAL
// ========================

// Verifica se o botão de fechar existe
if (btnFecharTransferencia) {

    // Adiciona o evento de clique ao botão de fechar
    btnFecharTransferencia.addEventListener("click", function() {

        // Esconde o modal alterando o display para "none"
        modalTransferencia.style.display = "none";
    });
}


// ========================
// 3. FECHAR MODAL AO CLICAR FORA
// ========================

// Adiciona um evento de clique em toda a janela
window.addEventListener("click", function(e) {

    // Verifica se o clique foi exatamente no fundo do modal
    // Ou seja, fora da caixa de conteúdo
    if (e.target === modalTransferencia) {

        // Fecha o modal
        modalTransferencia.style.display = "none";
    }
});

// Verifica se o botão de confirmar transferência existe na página
// Isso evita erro caso o script rode em uma tela onde o botão não existe
if (btnConfirmarTransferencia) {

    // Adiciona o evento de clique ao botão "Confirmar Transferência"
    btnConfirmarTransferencia.addEventListener("click", function() {

        // Captura o valor digitado no campo de número da conta de destino
        const contaDestinoInput = document.getElementById("numeroContaInput").value;

        // Captura o valor digitado no campo de valor da transferência (string)
        const valorInput = document.getElementById("valorTransferenciaInput").value;

        // Converte o valor digitado para número decimal (float)
        // Exemplo: "150.50" → 150.5
        const valor = parseFloat(valorInput);


        // ========================
        // 1. VALIDAÇÕES DE ENTRADA
        // ========================

        // Verifica se o número da conta de destino foi informado
        if (!contaDestinoInput) {
            alert("Digite o número da conta de destino.");
            return; // Interrompe a execução se estiver vazio
        }

        // Verifica se o valor é inválido, zero ou negativo
        if (!valor || valor <= 0) {
            alert("Valor inválido.");
            return; // Interrompe a execução se o valor não for válido
        }


        // ========================
        // 2. DADOS DO USUÁRIO LOGADO
        // ========================

        // Obtém o número da conta do usuário remetente salvo no localStorage
        const numeroContaRemetente = localStorage.getItem('usuarioId');

        if(contaDestinoInput == numeroContaRemetente){
            alert("Não pode realizar uma transferência para você mesmo")
            return;
        }

        // Obtém o token de autenticação salvo no localStorage
        const token = localStorage.getItem('token');

        // Garante que o token esteja no formato correto: "Bearer token"
        // Se já estiver formatado, mantém; senão, adiciona "Bearer "
        const tokenFormatado = token.startsWith("Bearer ")
            ? token
            : `Bearer ${token}`;


        // ========================
        // 3. OBJETO DA TRANSFERÊNCIA
        // ========================

        // Monta o objeto que será enviado para o backend
        const dadosTransferencia = {
            valor: valor, // valor da transferência
            numeroContaDestinatario: parseInt(contaDestinoInput) // conta destino como número inteiro
        };


        // ========================
        // 4. REQUISIÇÃO PARA O BACKEND
        // ========================

        // Envia a requisição para a API usando fetch
        fetch(`http://localhost:8080/conta/${numeroContaRemetente}/transferencia`, {

            // Define o método HTTP como PUT (atualização/operação)
            method: 'PUT',

            // Define os cabeçalhos da requisição
            headers: {
                'Authorization': tokenFormatado, // Token JWT
                'Content-Type': 'application/json' // Corpo em JSON
            },

            // Converte o objeto de dados para JSON
            body: JSON.stringify(dadosTransferencia)
        })


        // ========================
        // 5. TRATAMENTO DA RESPOSTA
        // ========================

        // Verifica se a resposta da API foi bem-sucedida (status 200–299)
        .then(response => {

            // Se deu certo, converte a resposta para JSON
            if (response.ok) return response.json();

            // Se deu erro, lê o texto e lança um erro personalizado
            return response.text().then(text => {
                throw new Error(text || "Erro na transferência");
            });
        })


        // ========================
        // 6. SUCESSO NA TRANSFERÊNCIA
        // ========================

        .then(() => {

            // Exibe mensagem de sucesso para o usuário
            alert("Transferência realizada com sucesso!");

            // Fecha o modal de transferência
            modalTransferencia.style.display = "none";

            // Recarrega os dados do dashboard, se a função existir
            if (typeof carregarDadosDoDashboard === "function") {
                carregarDadosDoDashboard(numeroContaRemetente, token);
            }

            // Atualiza o extrato, se a função existir
            if (typeof buscarExtrato === "function") {
                buscarExtrato(numeroContaRemetente, token);
            }
        })


        // ========================
        // 7. TRATAMENTO DE ERROS
        // ========================

        .catch(error => {

            // Exibe o erro no console para depuração
            console.error("Erro na transferência:", error);

            // Exibe mensagem de erro para o usuário
            alert("Falha na operação: " + error.message);
        });
    });
}

// ===============================
// --- LÓGICA DO EXTRATO DETALHADO ---
// ===============================

const modalExtrato = document.getElementById("modal-extrato");
const btnAbrirExtrato = document.getElementById("histórico"); // Botão "Extrato" do menu
const btnFecharExtrato = document.getElementById("fechar-extrato");

// 1. ABRIR O MODAL
if (btnAbrirExtrato) {
    btnAbrirExtrato.addEventListener("click", function(e) {
        e.preventDefault();
        modalExtrato.style.display = "flex";

        // Ao abrir, chama a função que desenha a tabela completa
        preencherExtratoDetalhado();
    });
}

// 2. FECHAR O MODAL
if (btnFecharExtrato) {
    btnFecharExtrato.addEventListener("click", function() {
        modalExtrato.style.display = "none";
    });
}

// Fecha clicando fora
window.addEventListener("click", function(e) {
    if (e.target === modalExtrato) {
        modalExtrato.style.display = "none";
    }
});

// --- FUNÇÃO QUE DESENHA A TABELA DETALHADA ---
function preencherExtratoDetalhado() {
    const tbody = document.getElementById("tbody-extrato-detalhado");
    const msgVazio = document.getElementById("msg-extrato-vazio");
    const meuId = localStorage.getItem('usuarioId'); // Necessário para saber se é entrada ou saída

    // Limpa a tabela antes de começar
    tbody.innerHTML = '';

    // Se a lista estiver vazia (ainda não carregou ou não tem dados)
    if (!todasTransacoesCache || todasTransacoesCache.length === 0) {
        if(msgVazio) msgVazio.style.display = "block";
        return;
    }

    if(msgVazio) msgVazio.style.display = "none";

    // Cria uma cópia da lista e inverte (mais recente primeiro)
    //const listaOrdenada = [...todasTransacoesCache];

    todasTransacoesCache.forEach(transacao => {

        // --- 1. TRATAMENTO DE DATA E HORA ---
        let dataHoraFormatada = "Inválido";

        if (transacao.data) {
            // Se vier como Array do Java: [2025, 12, 31, 14, 30, 00]
            if (Array.isArray(transacao.data)) {
                const [ano, mes, dia, hora, min] = transacao.data;
                const d = String(dia).padStart(2,'0');
                const m = String(mes).padStart(2,'0');
                // Verifica se hora existe, senão põe 00
                const h = hora !== undefined ? String(hora).padStart(2,'0') : '00';
                const mi = min !== undefined ? String(min).padStart(2,'0') : '00';

                dataHoraFormatada = `${d}/${m}/${ano} ${h}:${mi}`;
            }
            // Se vier como String ISO: "2025-12-31T14:30:00"
            else {
                const dataObj = new Date(transacao.data);
                if(!isNaN(dataObj)) {
                    dataHoraFormatada = dataObj.toLocaleString('pt-BR', {
                        day: '2-digit', month: '2-digit', year: 'numeric',
                        hour: '2-digit', minute: '2-digit'
                    });
                }
            }
        }

        // --- 2. TRATAMENTO DE VALOR ---
        const valorFormatado = transacao.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

        // --- 3. NOMES (Remetente e Destinatário) ---
        // Se for nulo (ex: saque não tem destinatário), colocamos um traço "-"
        // Se for depósito, o remetente pode ser nulo ou o banco
        const nomeRemetente = transacao.contaRemetente ? transacao.contaRemetente.nome : '-';
        const nomeDestinatario = transacao.contaDestinatario ? transacao.contaDestinatario.nome : '-';

        // --- 4. LÓGICA DE COR (VERDE OU VERMELHO) ---
        // Mesma lógica que fizemos antes: Se eu sou o remetente, o dinheiro saiu (Vermelho)
        let classeCor = "valor-entrada"; // Classe CSS definida para Verde

        const souEuRemetente = transacao.contaRemetente && String(transacao.contaRemetente.numeroDaConta) === String(meuId);

        if (transacao.tipo === 'SAQUE' || (transacao.tipo === 'TRANSFERENCIA' && souEuRemetente) || transacao.valor < 0) {
            classeCor = "valor-saida"; // Classe CSS definida para Vermelho
        }

        // --- 5. CRIAÇÃO DA LINHA HTML ---
        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>${dataHoraFormatada}</td>
            <td>${transacao.tipo}</td>
            <td>${nomeRemetente}</td>
            <td>${nomeDestinatario}</td>
            <td class="${classeCor}">
                ${valorFormatado}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

const linkVerTudo = document.getElementById("link-ver-tudo");

// Verifica se o link existe na página para evitar erros
if (linkVerTudo) {
    linkVerTudo.addEventListener("click", function(e) {
        e.preventDefault(); // Impede que a tela suba ou recarregue a página

        // 1. Abre o Modal de Extrato
        const modalExtrato = document.getElementById("modal-extrato");
        if(modalExtrato) {
            modalExtrato.style.display = "flex";
        }

        // 2. Chama a função que preenche a tabela detalhada
        // (Essa função nós criamos no passo anterior)
        if(typeof preencherExtratoDetalhado === "function") {
            preencherExtratoDetalhado();
        }
    });
}
