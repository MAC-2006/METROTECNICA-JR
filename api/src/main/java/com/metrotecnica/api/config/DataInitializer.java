package com.metrotecnica.api.config;

import com.metrotecnica.api.model.Tenant;
import com.metrotecnica.api.model.User;
import com.metrotecnica.api.repository.TenantRepository;
import com.metrotecnica.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        criarSuperAdmin();
        criarTenantDeTesteComAdmin();
    }

    // ==========================================================
    // SUPER-ADMIN GLOBAL: sem tenant_id, gerencia todas as empresas
    // (equivalente ao miguelazecosta@gmail.com do MVP em Flask)
    // ==========================================================
    private void criarSuperAdmin() {
        String email = "superadmin@metrotecnica.com";

        if (userRepository.findByEmail(email).isEmpty()) {
            User superAdmin = new User();
            superAdmin.setEmail(email);
            superAdmin.setPassword(passwordEncoder.encode("super123"));
            superAdmin.setRole("admin");
            superAdmin.setCanSign(false);
            superAdmin.setNomeCompleto("Administrador Geral Metrotécnica");
            // tenant propositalmente null: é o super-admin global

            userRepository.save(superAdmin);
            System.out.println("✅ Super-admin global criado: " + email + " / senha: super123 (sem tenant — gerencia todas as empresas)");
        }
    }

    // ==========================================================
    // TENANT DE TESTE + admin vinculado a ele (para testar o fluxo
    // de instrumentos de uma empresa específica)
    // ==========================================================
    private void criarTenantDeTesteComAdmin() {
        Tenant tenantTeste = tenantRepository.findBySlug("teste").orElseGet(() -> {
            Tenant t = new Tenant();
            t.setName("Empresa Teste");
            t.setRazaoSocial("Empresa Teste Ltda");
            t.setCnpj("00000000000100");
            t.setSlug("teste");
            Tenant salvo = tenantRepository.save(t);
            System.out.println("✅ Tenant de teste criado: " + salvo.getName() + " (id=" + salvo.getId() + ")");
            return salvo;
        });

        String emailAdmin = "admin@metrotecnica.com";
        User admin = userRepository.findByEmail(emailAdmin).orElse(null);

        if (admin == null) {
            admin = new User();
            admin.setEmail(emailAdmin);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("admin");
            admin.setCanSign(true);
            admin.setNomeCompleto("Administrador Sistema");
            admin.setTenant(tenantTeste);

            userRepository.save(admin);
            System.out.println("✅ Usuário admin (tenant) criado: " + emailAdmin + " / senha: admin123 / tenant: " + tenantTeste.getSlug());
        } else if (admin.getTenant() == null) {
            admin.setTenant(tenantTeste);
            userRepository.save(admin);
            System.out.println("✅ Usuário admin já existia — vinculado ao tenant de teste agora.");
        }
    }
}