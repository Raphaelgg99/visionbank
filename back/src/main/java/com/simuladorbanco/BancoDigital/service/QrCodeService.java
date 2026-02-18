package com.simuladorbanco.BancoDigital.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QrCodeService {

    public String gerarQrCodeBase64(String textoParaEsconder, int largura, int altura) {
        try {
            // 1. Prepara a "caneta" que vai desenhar o QR Code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // 2. Desenha a matriz (os quadradinhos pretos e brancos)
            BitMatrix bitMatrix = qrCodeWriter.encode(textoParaEsconder, BarcodeFormat.QR_CODE, largura, altura);

            // 3. Prepara um espaço na memória para salvar a imagem
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // 4. Salva a matriz gerada no formato PNG dentro dessa memória
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            // 5. Transforma a imagem (bytes) em um texto Base64
            byte[] imagemBytes = outputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imagemBytes);

            // Devolve o texto pronto para o HTML usar
            return "data:image/png;base64," + base64Image;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar o QR Code: " + e.getMessage());
        }
    }
}