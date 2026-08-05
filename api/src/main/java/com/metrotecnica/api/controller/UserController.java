package com.metrotecnica.api.controller;

import com.metrotecnica.api.dto.UserRequestDTO;
import com.metrotecnica.api.dto.UserResponseDTO;
import com.metrotecnica.api.dto.UserUpdateDTO;
import com.metrotecnica.api.model.Tenant;
import com.metrotecnica.api.model.User;
import com.metrotecnica.api.repository.TenantRepository;
import com.metrotecnica.api.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ==========================================================
// /api/tenants/{tenantId}/users
//
// Gestão de usuários de uma empresa (tenant) específica.
// Assim como o TenantController, esta rota é exclusiva do
// super-admin (usuário com role ADMIN e sem tenant vinculado
// no token) — ele quem cria os logins que cada empresa vai usar.
// ==========================================================
@RestController
@RequestMapping("/api/tenants/{tenantId}/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') and authentication.principal.tenantId == null")
public class UserController {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    // ==========================================================
    // GET /api/tenants/{tenantId}/users — lista os usuários da empresa
    // ==========================================================
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listar(@PathVariable Long tenantId) {
        garantirTenantExiste(tenantId);

        List<UserResponseDTO> usuarios = userRepository.findByTenantIdOrderByEmailAsc(tenantId)
                .stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(usuarios);
    }

    // ==========================================================
    // POST /api/tenants/{tenantId}/users — cria um novo usuário
    // ==========================================================
    @PostMapping
    public ResponseEntity<?> criar(@PathVariable Long tenantId, @Valid @RequestBody UserRequestDTO dto) {
        Tenant tenant = garantirTenantExiste(tenantId);

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Já existe um usuário com esse e-mail"));
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole((dto.getRole() == null || dto.getRole().isBlank()) ? "user" : dto.getRole());
        user.setNomeCompleto(dto.getNomeCompleto());
        user.setCanSign(dto.getCanSign() != null ? dto.getCanSign() : false);
        user.setTenant(tenant);

        User salvo = userRepository.save(user);
        return ResponseEntity.status(201).body(UserResponseDTO.fromEntity(salvo));
    }

    // ==========================================================
    // PUT /api/tenants/{tenantId}/users/{id} — atualiza um usuário
    // (a senha só é alterada se vier preenchida no corpo da requisição)
    // ==========================================================
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long tenantId,
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO dto
    ) {
        garantirTenantExiste(tenantId);
        User user = buscarUsuarioDoTenant(tenantId, id);

        if (dto.getNomeCompleto() != null) user.setNomeCompleto(dto.getNomeCompleto());
        if (dto.getRole() != null && !dto.getRole().isBlank()) user.setRole(dto.getRole());
        if (dto.getCanSign() != null) user.setCanSign(dto.getCanSign());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User salvo = userRepository.save(user);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(salvo));
    }

    // ==========================================================
    // DELETE /api/tenants/{tenantId}/users/{id} — remove um usuário
    // ==========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long tenantId, @PathVariable Long id) {
        garantirTenantExiste(tenantId);
        User user = buscarUsuarioDoTenant(tenantId, id);

        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("message", "Usuário removido com sucesso"));
    }

    private Tenant garantirTenantExiste(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));
    }

    private User buscarUsuarioDoTenant(Long tenantId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.getTenant() == null || !user.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Usuário não pertence a esta empresa");
        }

        return user;
    }
}
