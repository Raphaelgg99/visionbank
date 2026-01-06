package com.simuladorbanco.BancoDigital.controller;

import com.simuladorbanco.BancoDigital.dtos.TransacaoDTO;
import com.simuladorbanco.BancoDigital.dtos.TransferenciaRequest;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.service.ContaService;
import com.simuladorbanco.BancoDigital.service.HistoricoService;
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

    @PutMapping("/{numeroDaConta}/depositar")
    @Transactional
    public ResponseEntity<TransacaoDTO> depositar(@RequestBody Double valor, @PathVariable Long numeroDaConta) {
       return ResponseEntity.ok(contaService.depositar(valor, numeroDaConta));
    }

    @PutMapping("/{numeroDaConta}/sacar")
    public ResponseEntity <TransacaoDTO> sacar(@RequestBody double valor, @PathVariable Long numeroDaConta){
        return ResponseEntity.ok(contaService.sacar(valor, numeroDaConta));
    }

    @PutMapping("/{numeroContaRemetente}/transferencia")
    public ResponseEntity <TransacaoDTO> transferencia(@RequestBody TransferenciaRequest transferencia,
                                              @PathVariable Long numeroContaRemetente){
        return ResponseEntity.ok(contaService.transferencia(transferencia, numeroContaRemetente));
    }

    @PostMapping("/adicionar")
    public ResponseEntity<Conta> adicionarConta(@RequestBody Conta conta) {
        return ResponseEntity.ok(contaService.criarConta(conta));
    }

    @PutMapping("/{numeroDaConta}/atualizar")
    public ResponseEntity<Conta> atualizarConta(@PathVariable Long numeroDaConta, @RequestBody Conta contaAtualizada){
        return ResponseEntity.ok(contaService.atualizarConta(numeroDaConta, contaAtualizada));
    }

    @DeleteMapping("/{numeroDaConta}")
    public ResponseEntity<String> removerConta(@PathVariable Long numeroDaConta){
        contaService.removerConta(numeroDaConta);
        return ResponseEntity.ok("Conta removida com sucesso");
    }

    @GetMapping("/listartodas")
    public ResponseEntity <List<Conta>> listarTodos(){
        return ResponseEntity.ok(contaService.listarTodos());
    }

    @GetMapping("/{numeroDaConta}/extrato")
    public ResponseEntity <List<TransacaoDTO>> getHistoricoConta(@PathVariable Long numeroDaConta) {
        return ResponseEntity.ok(historicoService.listarTodasAsTransacoes(numeroDaConta));
    }

    // Método para buscar os dados do usuário pelo ID
    @GetMapping("/{numeroDaConta}")
    public ResponseEntity<Conta> buscarConta(@PathVariable Long numeroDaConta) {
        // Aqui estamos chamando o service.
        // Se seu service não tiver o método 'buscarPorId', precisaremos criar lá também.
        Conta conta = contaService.buscarConta(numeroDaConta);
        return ResponseEntity.ok(conta);
    }
}
