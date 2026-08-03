package com.metrotecnica.api.controller;

import com.metrotecnica.api.dto.SetorLocalDTO;
import com.metrotecnica.api.model.LocalUso;
import com.metrotecnica.api.model.Tenant;
import com.metrotecnica.api.repository.LocalUsoRepository;
import com.metrotecnica.api.repository.TenantRepository;
import com.metrotecnica.api.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locais-uso")
@RequiredArgsConstructor
public class LocalUsoController {

    private final LocalUsoRepository localUsoRepository;
    private final TenantRepository tenantRepository;

    @GetMapping
    public ResponseEntity<List<LocalUso>> listar(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(localUsoRepository.findByTenantIdOrderByNomeAsc(user.getTenantId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criar(@Valid @RequestBody SetorLocalDTO dto, @AuthenticationPrincipal UserPrincipal user) {
        if (localUsoRepository.findByNomeAndTenantId(dto.getNome(), user.getTenantId()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Já existe um local de uso com esse nome"));
        }

        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));

        LocalUso local = new LocalUso();
        local.setNome(dto.getNome());
        local.setTenant(tenant);

        LocalUso salvo = localUsoRepository.save(local);
        return ResponseEntity.status(201).body(Map.of("message", "Criado", "id", salvo.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody SetorLocalDTO dto,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        LocalUso local = localUsoRepository.findById(id)
                .filter(l -> l.getTenant() != null && l.getTenant().getId().equals(user.getTenantId()))
                .orElseThrow(() -> new RuntimeException("Local de uso não encontrado"));

        local.setNome(dto.getNome());
        localUsoRepository.save(local);

        return ResponseEntity.ok(Map.of("message", "Atualizado com sucesso"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal user) {
        LocalUso local = localUsoRepository.findById(id)
                .filter(l -> l.getTenant() != null && l.getTenant().getId().equals(user.getTenantId()))
                .orElseThrow(() -> new RuntimeException("Local de uso não encontrado"));

        localUsoRepository.delete(local);
        return ResponseEntity.ok(Map.of("message", "Removido com sucesso"));
    }
}