package com.simuladorbanco.BancoDigital.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private TransacaoService transacaoService;

    public Map<String, String> gerarQrCodePix(Long numeroContaDestinatario, BigDecimal valor) {
        try {
            // Define que o QR Code vale por 30 minutos a partir de agora
            LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(30);

            // Montamos um Map e usamos o Jackson para criar um JSON perfeito e limpo
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("numeroContaDestinatario", numeroContaDestinatario);
            payloadMap.put("valor", valor);
            payloadMap.put("expiracao", dataExpiracao.toString()); // Guarda a hora limite

            // Transforma o Map em texto JSON: {"contaDestino":129,"valor":50.0,"expiracao":"2026-02-18T16:30:00"}
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

    public Transacao pagarQrCodePix(Long numeroContaRemetente, String conteudoQrCode) {
        try {
            // 1. Extrai os dados do texto do QR Code de forma elegante com Jackson
            JsonNode jsonNode = objectMapper.readTree(conteudoQrCode);
            Long contaRecebedoraId = jsonNode.get("numeroContaDestinatario").asLong();
            double valorPix = jsonNode.get("valor").asDouble();
            String expiracaoStr = jsonNode.get("expiracao").asText();
            LocalDateTime dataExpiracao = LocalDateTime.parse(expiracaoStr);

            // A MÁGICA DE SEGURANÇA: Verifica se o momento atual é DEPOIS da data de expiração
            if (LocalDateTime.now().isAfter(dataExpiracao)) {
                throw new RuntimeException("Este QR Code do PIX já expirou!");
            }

            // 2. Busca e valida as contas
            Conta contaRemetente = contaRepository.findById(numeroContaRemetente)
                    .orElseThrow(() -> new RuntimeException("Sua conta não foi encontrada"));

            Conta contaDestinatario = contaRepository.findById(contaRecebedoraId)
                    .orElseThrow(() -> new RuntimeException("Conta destino do QR Code inválida"));

            // 3. Efetiva a transação usando o seu TransacaoService intocável
            return transacaoService.adicionarTransacaoTransferencia(contaRemetente, contaDestinatario, valorPix);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar o pagamento do PIX: " + e.getMessage());
        }
    }
}