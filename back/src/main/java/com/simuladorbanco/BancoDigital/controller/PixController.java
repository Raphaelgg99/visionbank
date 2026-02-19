package com.simuladorbanco.BancoDigital.controller;

import com.simuladorbanco.BancoDigital.dtos.TransacaoDTO;
import com.simuladorbanco.BancoDigital.model.Transacao;
import com.simuladorbanco.BancoDigital.service.PixService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/pix")
@CrossOrigin(origins = "*")
// Dando o destaque que o PIX merece!
@Tag(name = "4. PIX", description = "Geração e Pagamento via PIX (Leitura de QR Code e Copia/Cola)")
public class PixController {

    @Autowired
    private PixService pixService;

    @PostMapping("/gerar/{numeroContaDestinatario}")
    @Operation(summary = "Gera uma cobrança PIX (QR Code)", description = "Cria um payload contendo os dados do destinatário, valor e trava de validade (expiração em 30 minutos). Retorna a string do QR Code e os dados formatados para conferência.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR Code gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta de destino não encontrada")
    })
    public ResponseEntity<Map<String, String>> gerarQrCode(
            @PathVariable Long numeroContaDestinatario,
            @RequestBody Map<String, BigDecimal> request) {

        BigDecimal valor = request.get("valor");
        Map<String, String> response = pixService.gerarQrCodePix(numeroContaDestinatario, valor);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/pagar/{numeroDaContaRemetente}")
    @Operation(summary = "Realiza pagamento de PIX", description = "Processa o texto de um QR Code lido pela câmera ou 'Pix Copia e Cola'. Valida a data de expiração, verifica o saldo do remetente e efetiva a transação instantânea usando lógica ACID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PIX pago com sucesso e saldos atualizados"),
            @ApiResponse(responseCode = "400", description = "QR Code corrompido, expirado ou saldo insuficiente"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão (Token ausente/inválido)")
    })
    public ResponseEntity<?> pagarQrCode(
            @PathVariable Long numeroDaContaRemetente,
            @RequestBody Map<String, String> request) {

        try {
            String conteudoQrCode = request.get("qrCodeTexto");
            TransacaoDTO dto = pixService.pagarQrCodePix(numeroDaContaRemetente, conteudoQrCode);

            return ResponseEntity.ok("PIX de R$ " + dto.getValor() + " pago com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

