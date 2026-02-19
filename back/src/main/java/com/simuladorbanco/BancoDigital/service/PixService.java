package com.simuladorbanco.BancoDigital.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simuladorbanco.BancoDigital.dtos.TransacaoDTO;
import com.simuladorbanco.BancoDigital.dtos.TransferenciaRequest;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.model.Transacao;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PixService {

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContaService contaService;

    public Map<String, String> gerarQrCodePix(Long numeroContaDestinatario, BigDecimal valor) {
        try {
            Conta contaDestinatario = contaRepository.findById(numeroContaDestinatario)
                    .orElseThrow(() -> new RuntimeException("Conta destino não encontrada"));
            LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(30);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("nomeDestinatario", contaDestinatario.getNome());
            payloadMap.put("numeroContaDestinatario", numeroContaDestinatario);
            payloadMap.put("valor", valor);
            payloadMap.put("expiracao", dataExpiracao.toString());

            String payloadPix = objectMapper.writeValueAsString(payloadMap);

            // Manda desenhar a imagem
            String imagemBase64 = qrCodeService.gerarQrCodeBase64(payloadPix, 300, 300);

            Map<String, String> response = new HashMap<>();
            response.put("qrCodeBase64", imagemBase64);
            response.put("conteudo", payloadPix);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PIX: " + e.getMessage());
        }
    }

    public TransacaoDTO pagarQrCodePix(Long numeroContaRemetente, String conteudoQrCode) {
        try {
            JsonNode jsonNode = objectMapper.readTree(conteudoQrCode);
            Long numeroContaDestinatario = jsonNode.get("numeroContaDestinatario").asLong();
            double valorPix = jsonNode.get("valor").asDouble();
            String expiracaoStr = jsonNode.get("expiracao").asText();
            LocalDateTime dataExpiracao = LocalDateTime.parse(expiracaoStr);

            // A MÁGICA DE SEGURANÇA: Verifica se o momento atual é DEPOIS da data de expiração
            if (LocalDateTime.now().isAfter(dataExpiracao)) {
                throw new RuntimeException("Este QR Code do PIX já expirou!");
            }

            TransferenciaRequest request = new TransferenciaRequest();
            request.setValor(valorPix);
            request.setNumeroContaDestinatario(numeroContaDestinatario);

            return contaService.transferencia(request,numeroContaRemetente);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar o pagamento do PIX: " + e.getMessage());
        }
    }
}