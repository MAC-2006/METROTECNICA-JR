package com.metrotecnica.api.controller;

import com.metrotecnica.api.dto.LoginResponse;
import com.metrotecnica.api.dto.TenantRequestDTO;
import com.metrotecnica.api.model.Tenant;
import com.metrotecnica.api.repository.TenantRepository;
import com.metrotecnica.api.security.JwtService;
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
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') and authentication.principal.tenantId == null")
public class TenantController {

    private final TenantRepository tenantRepository;
    private final JwtService jwtService;

    // ==========================================================
    // GET /api/tenants — lista todos (uso administrativo)
    // ==========================================================
    @GetMapping
    public ResponseEntity<List<Tenant>> listar() {
        return ResponseEntity.ok(tenantRepository.findAll());
    }

    // ==========================================================
    // GET /api/tenants/{id}
    // ==========================================================
    @GetMapping("/{id}")
    public ResponseEntity<Tenant> obterPorId(@PathVariable Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));
        return ResponseEntity.ok(tenant);
    }

    // ==========================================================
    // POST /api/tenants
    // ==========================================================
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody TenantRequestDTO dto) {
        if (tenantRepository.findBySlug(dto.getSlug()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Já existe um tenant com esse slug"));
        }

        Tenant tenant = new Tenant();
        aplicarCampos(tenant, dto);

        Tenant salvo = tenantRepository.save(tenant);
        return ResponseEntity.status(201).body(Map.of("message", "Criado", "id", salvo.getId()));
    }

    // ==========================================================
    // PUT /api/tenants/{id}
    // ==========================================================
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody TenantRequestDTO dto) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));

        aplicarCampos(tenant, dto);
        tenantRepository.save(tenant);

        return ResponseEntity.ok(Map.of("message", "Atualizado com sucesso"));
    }

    // ==========================================================
    // POST /api/tenants/{id}/impersonate
    // Gera um novo token JWT escopado para o tenant escolhido, mantendo
    // o e-mail e o papel (admin) de quem está chamando.
    // ==========================================================
    @PostMapping("/{id}/impersonate")
    public ResponseEntity<?> impersonar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal usuarioAtual
    ) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));

        // Bloqueia impersonate se a empresa ainda não tiver dados jurídicos completos.
        if (tenant.getRazaoSocial() == null || tenant.getRazaoSocial().isBlank()
                || tenant.getCnpj() == null || tenant.getCnpj().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "cadastro_incompleto",
                    "message", "Esta empresa possui dados jurídicos pendentes.",
                    "tenantId", tenant.getId(),
                    "name", tenant.getName()
            ));
        }

        String token = jwtService.generateToken(usuarioAtual.getEmail(), "admin", tenant.getId());
        return ResponseEntity.ok(new LoginResponse(token, "admin"));
    }

    private void aplicarCampos(Tenant tenant, TenantRequestDTO dto) {
        tenant.setName(dto.getName());
        tenant.setRazaoSocial(dto.getRazaoSocial());
        tenant.setCnpj(dto.getCnpj());
        tenant.setLogradouro(dto.getLogradouro());
        tenant.setNumero(dto.getNumero());
        tenant.setBairro(dto.getBairro());
        tenant.setCidade(dto.getCidade());
        tenant.setEstado(dto.getEstado());
        tenant.setCep(dto.getCep());
        tenant.setTelefone(dto.getTelefone());
        tenant.setSlug(dto.getSlug());
        tenant.setUrl(dto.getUrl());
    }
}