package com.metrotecnica.api.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.metrotecnica.api.model.Instrumento;
import com.metrotecnica.api.repository.InstrumentoRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CertificadoService {

    private final InstrumentoRepository instrumentoRepository;
    private final TemplateEngine templateEngine;

    public byte[] gerarPdf(Long id, Long tenantId, String baseUrl) {
        Instrumento inst = instrumentoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Instrumento não encontrado"));

        Context context = new Context();
        context.setVariable("inst", inst);

        if (inst.getDocumentHash() != null) {
            String linkValidacao = baseUrl.replaceAll("/$", "") + "/validar/" + inst.getDocumentHash();
            String qrBase64 = gerarQrCodeBase64(linkValidacao);
            context.setVariable("qrCode", "data:image/png;base64," + qrBase64);
        } else {
            context.setVariable("qrCode", null);
        }

        String html = templateEngine.process("certificados/certificado", context);
        return converterParaPdf(html, baseUrl);
    }

    private String gerarQrCodeBase64(String texto) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar QR code", e);
        }
    }

    private byte[] converterParaPdf(String html, String baseUrl) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, baseUrl);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }
}