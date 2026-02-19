package com.simuladorbanco.BancoDigital.controller;

import com.simuladorbanco.BancoDigital.dtos.TransacaoDTO;
import com.simuladorbanco.BancoDigital.model.Transacao;
import com.simuladorbanco.BancoDigital.service.PixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/pix")
@CrossOrigin(origins = "*")
public class PixController {

    @Autowired
    private PixService pixService;

    @PostMapping("/gerar/{numeroContaDestinatario}")
    public ResponseEntity<Map<String, String>> gerarQrCode(
            @PathVariable Long numeroContaDestinatario,
            @RequestBody Map<String, BigDecimal> request) {

        BigDecimal valor = request.get("valor");

        // Vai direto no Service gerar a imagem, SEM encostar no banco de dados!
        Map<String, String> response = pixService.gerarQrCodePix(numeroContaDestinatario, valor);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/pagar/{numeroDaContaRemetente}")
    public ResponseEntity<?> pagarQrCode(
            @PathVariable Long numeroDaContaRemetente,
            @RequestBody Map<String, String> request) {

        try {
            String conteudoQrCode = request.get("qrCodeTexto");

            // O Controller não sabe de nada, só repassa a bola pro Service!
            TransacaoDTO dto = pixService.pagarQrCodePix(numeroDaContaRemetente, conteudoQrCode);

            return ResponseEntity.ok("PIX de R$ " + dto.getValor() + " pago com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

