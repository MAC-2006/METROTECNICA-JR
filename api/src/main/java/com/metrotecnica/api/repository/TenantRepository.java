package com.metrotecnica.api.repository;

import com.metrotecnica.api.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findBySlug(String slug);
    Optional<Tenant> findByCnpj(String cnpj);
}