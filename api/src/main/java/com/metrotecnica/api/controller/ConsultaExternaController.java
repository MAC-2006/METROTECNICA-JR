package com.metrotecnica.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
@RequestMapping("/api/consulta")
public class ConsultaExternaController {

    private final RestClient restClient = RestClient.create();

    @GetMapping("/cnpj/{cnpj}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> buscarCnpj(@PathVariable String cnpj) {
        try {
            Map<?, ?> dados = restClient.get()
                    .uri("https://brasilapi.com.br/api/cnpj/v1/{cnpj}", cnpj)
                    .retrieve()
                    .body(Map.class);
            return ResponseEntity.ok(dados);
        } catch (Exception e) {
            // Log real do que aconteceu — antes isso era engolido no navegador
            System.err.println("Falha ao consultar CNPJ " + cnpj + ": " + e.getMessage());
            return ResponseEntity.status(502).body(Map.of("error", "Não foi possível consultar o CNPJ: " + e.getMessage()));
        }
    }

    @GetMapping("/cep/{cep}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> buscarCep(@PathVariable String cep) {
        try {
            Map<?, ?> dados = restClient.get()
                    .uri("https://viacep.com.br/ws/{cep}/json/", cep)
                    .retrieve()
                    .body(Map.class);
            return ResponseEntity.ok(dados);
        } catch (Exception e) {
            System.err.println("Falha ao consultar CEP " + cep + ": " + e.getMessage());
            return ResponseEntity.status(502).body(Map.of("error", "Não foi possível consultar o CEP: " + e.getMessage()));
        }
    }
}