package com.metrotecnica.api.controller;

import com.metrotecnica.api.security.UserPrincipal;
import com.metrotecnica.api.service.CertificadoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CertificadoController {

    private final CertificadoService certificadoService;

    @GetMapping("/api/certificado/{id}/pdf")
    public ResponseEntity<byte[]> gerarCertificado(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user,
            HttpServletRequest request
    ) {
        String baseUrl = request.getRequestURL().toString().replaceAll("/api/.*", "");
        byte[] pdf = certificadoService.gerarPdf(id, user.getTenantId(), baseUrl);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=certificado.pdf")
                .body(pdf);
    }
}