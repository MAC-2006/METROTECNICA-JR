package com.metrotecnica.api.controller;

import com.metrotecnica.api.service.PadraoStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PadraoController {

    private final PadraoStorageService storageService;

    @GetMapping("/lista-padroes")
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(Map.of("arquivos", storageService.listarArquivos()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/busca-padroes")
    public ResponseEntity<?> buscar(@RequestParam("q") String termo) {
        try {
            return ResponseEntity.ok(Map.of("arquivos", storageService.buscar(termo)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/padroes/download")
    public ResponseEntity<FileSystemResource> download(@RequestParam("arquivo") String arquivo) {
        Path caminho = storageService.resolverArquivo(arquivo);
        FileSystemResource resource = new FileSystemResource(caminho);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + caminho.getFileName() + "\"")
                .body(resource);
    }

    @PostMapping("/admin/upload-padroes")
    @PreAuthorize("hasRole('ADMIN') and authentication.principal.tenantId == null")
    public ResponseEntity<?> uploadPadroes(@RequestParam("file") MultipartFile file) {
        try {
            int count = storageService.importarZip(file);
            return ResponseEntity.ok(Map.of("message", "Padrões importados com sucesso", "count", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}