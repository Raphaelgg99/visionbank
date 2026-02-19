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
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            BitMatrix bitMatrix = qrCodeWriter.encode(textoParaEsconder, BarcodeFormat.QR_CODE, largura, altura);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] imagemBytes = outputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imagemBytes);
            return "data:image/png;base64," + base64Image;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar o QR Code: " + e.getMessage());
        }
    }
}