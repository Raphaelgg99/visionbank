package com.simuladorbanco.BancoDigital.controller;

import com.simuladorbanco.BancoDigital.dtos.TransacaoDTO;
import com.simuladorbanco.BancoDigital.dtos.TransferenciaRequest;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.service.ContaService;
import com.simuladorbanco.BancoDigital.service.HistoricoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conta")
@CrossOrigin(origins = "*")
public class ContaController {

    @Autowired
    ContaService contaService;

    @Autowired
    HistoricoService historicoService;

    // ==========================================
    // OPERAÇÕES FINANCEIRAS (Tag 3)
    // ==========================================

    @PutMapping("/{numeroDaConta}/depositar")
    @Transactional
    @Operation(summary = "Realiza um depósito", description = "Adiciona o valor especificado ao saldo da conta. Não exige token (simula depósito em caixa eletrônico).", tags = {"3. Operações Financeiras"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<TransacaoDTO> depositar(@RequestBody Double valor, @PathVariable Long numeroDaConta) {
        return ResponseEntity.ok(contaService.depositar(valor, numeroDaConta));
    }

    @PutMapping("/{numeroDaConta}/sacar")
    @Operation(summary = "Realiza um saque", description = "Deduz o valor do saldo da conta informada. Exige autenticação e saldo suficiente.", tags = {"3. Operações Financeiras"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saque aprovado e saldo atualizado"),
            @ApiResponse(responseCode = "400", description = "Saldo insuficiente para o saque"),
            @ApiResponse(responseCode = "403", description = "Usuário não autenticado")
    })
    public ResponseEntity<TransacaoDTO> sacar(@RequestBody double valor, @PathVariable Long numeroDaConta) {
        return ResponseEntity.ok(contaService.sacar(valor, numeroDaConta));
    }

    @PutMapping("/{numeroContaRemetente}/transferencia")
    @Operation(summary = "Transferência entre contas", description = "Transfere um valor da conta do remetente para uma conta de destino. Valida saldo e atualiza ambas as contas em uma única transação.", tags = {"3. Operações Financeiras"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Saldo insuficiente ou mesma conta de destino")
    })
    public ResponseEntity<TransacaoDTO> transferencia(@RequestBody TransferenciaRequest transferencia, @PathVariable Long numeroContaRemetente) {
        return ResponseEntity.ok(contaService.transferencia(transferencia, numeroContaRemetente));
    }

    @GetMapping("/{numeroDaConta}/extrato")
    @Operation(summary = "Emite o extrato", description = "Retorna a lista completa de todas as transações (entradas e saídas) associadas à conta.", tags = {"3. Operações Financeiras"})
    public ResponseEntity<List<TransacaoDTO>> getHistoricoConta(@PathVariable Long numeroDaConta) {
        return ResponseEntity.ok(historicoService.listarTodasAsTransacoes(numeroDaConta));
    }

    // ==========================================
    // GERENCIAMENTO DE CONTAS (Tag 2 - Herdada da Classe)
    // ==========================================

    @PostMapping("/adicionar")
    @Operation(summary = "Abre uma nova conta", description = "Cria um novo registro de usuário e conta bancária com saldo inicial zero.", tags = {"2. Gerenciamento de Contas"})
    public ResponseEntity<Conta> adicionarConta(@RequestBody Conta conta) {
        return ResponseEntity.ok(contaService.criarConta(conta));
    }

    @PutMapping("/{numeroDaConta}/atualizar")
    @Operation(summary = "Atualiza dados cadastrais", description = "Altera informações de uma conta existente pelo ID.", tags = {"2. Gerenciamento de Contas"})
    public ResponseEntity<Conta> atualizarConta(@PathVariable Long numeroDaConta, @RequestBody Conta contaAtualizada) {
        return ResponseEntity.ok(contaService.atualizarConta(numeroDaConta, contaAtualizada));
    }

    @DeleteMapping("/{numeroDaConta}")
    @Operation(summary = "Encerra a conta", description = "Remove permanentemente a conta e seus vínculos do banco de dados.", tags = {"2. Gerenciamento de Contas"})
    public ResponseEntity<String> removerConta(@PathVariable Long numeroDaConta) {
        contaService.removerConta(numeroDaConta);
        return ResponseEntity.ok("Conta removida com sucesso");
    }

    @GetMapping("/listartodas")
    @Operation(summary = "Lista todas as contas", description = "Retorna um array com todas as contas ativas no banco de dados. (Uso restrito para administração).", tags = {"2. Gerenciamento de Contas"})
    public ResponseEntity<List<Conta>> listarTodos() {
        return ResponseEntity.ok(contaService.listarTodos());
    }

    @GetMapping("/{numeroDaConta}")
    @Operation(summary = "Busca conta específica", description = "Retorna os detalhes de uma única conta a partir do seu número de identificação.", tags = {"2. Gerenciamento de Contas"})
    public ResponseEntity<Conta> buscarConta(@PathVariable Long numeroDaConta) {
        Conta conta = contaService.buscarConta(numeroDaConta);
        return ResponseEntity.ok(conta);
    }
}
