package com.metrotecnica.api.controller;

import com.metrotecnica.api.dto.SetorLocalDTO;
import com.metrotecnica.api.model.Setor;
import com.metrotecnica.api.model.Tenant;
import com.metrotecnica.api.repository.SetorRepository;
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
@RequestMapping("/api/setores")
@RequiredArgsConstructor
public class SetorController {

    private final SetorRepository setorRepository;
    private final TenantRepository tenantRepository;

    // ==========================================================
    // GET /api/setores — lista os setores do tenant do usuário logado
    // ==========================================================
    @GetMapping
    public ResponseEntity<List<Setor>> listar(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(setorRepository.findByTenantIdOrderByNomeAsc(user.getTenantId()));
    }

    // ==========================================================
    // POST /api/setores
    // ==========================================================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criar(@Valid @RequestBody SetorLocalDTO dto, @AuthenticationPrincipal UserPrincipal user) {
        if (setorRepository.findByNomeAndTenantId(dto.getNome(), user.getTenantId()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Já existe um setor com esse nome"));
        }

        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));

        Setor setor = new Setor();
        setor.setNome(dto.getNome());
        setor.setTenant(tenant);

        Setor salvo = setorRepository.save(setor);
        return ResponseEntity.status(201).body(Map.of("message", "Criado", "id", salvo.getId()));
    }

    // ==========================================================
    // PUT /api/setores/{id}
    // ==========================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody SetorLocalDTO dto,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Setor setor = setorRepository.findById(id)
                .filter(s -> s.getTenant() != null && s.getTenant().getId().equals(user.getTenantId()))
                .orElseThrow(() -> new RuntimeException("Setor não encontrado"));

        setor.setNome(dto.getNome());
        setorRepository.save(setor);

        return ResponseEntity.ok(Map.of("message", "Atualizado com sucesso"));
    }

    // ==========================================================
    // DELETE /api/setores/{id}
    // ==========================================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal user) {
        Setor setor = setorRepository.findById(id)
                .filter(s -> s.getTenant() != null && s.getTenant().getId().equals(user.getTenantId()))
                .orElseThrow(() -> new RuntimeException("Setor não encontrado"));

        setorRepository.delete(setor);
        return ResponseEntity.ok(Map.of("message", "Removido com sucesso"));
    }
}