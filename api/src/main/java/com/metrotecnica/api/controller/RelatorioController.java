package com.metrotecnica.api.controller;

import com.metrotecnica.api.security.UserPrincipal;
import com.metrotecnica.api.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/relatorio/pdf")
    public ResponseEntity<byte[]> gerarRelatorio(
            @RequestParam String tipo,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) String valor,
            @RequestParam(required = false, defaultValue = "false") boolean download,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        byte[] pdf = relatorioService.gerarPdf(tipo, start, end, valor, user.getTenantId());
        String disposicao = (download ? "attachment" : "inline") + "; filename=relatorio.pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao)
                .body(pdf);
    }

    @GetMapping("/relatorio/excel")
    public ResponseEntity<byte[]> gerarRelatorioExcel(
            @RequestParam String tipo,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) String valor,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        byte[] excel = relatorioService.gerarExcel(tipo, start, end, valor, user.getTenantId());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio.xlsx")
                .body(excel);
    }
}