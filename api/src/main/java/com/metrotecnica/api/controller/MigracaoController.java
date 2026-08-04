package com.metrotecnica.api.controller;

import com.metrotecnica.api.dto.MigracaoResponseDTO;
import com.metrotecnica.api.service.MigracaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class MigracaoController {

    private final MigracaoService migracaoService;

    @PostMapping("/upload-migracao")
    @PreAuthorize("hasRole('ADMIN') and authentication.principal.tenantId == null")
    public ResponseEntity<?> uploadMigracao(
            @RequestParam("file") MultipartFile file,
            @RequestParam("nome_empresa") String nomeEmpresa
    ) {
        try {
            MigracaoResponseDTO resultado = migracaoService.migrar(file, nomeEmpresa);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}