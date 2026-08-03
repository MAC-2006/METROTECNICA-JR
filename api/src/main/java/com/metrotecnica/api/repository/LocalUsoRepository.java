package com.metrotecnica.api.repository;

import com.metrotecnica.api.model.LocalUso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LocalUsoRepository extends JpaRepository<LocalUso, Long> {
    List<LocalUso> findByTenantIdOrderByNomeAsc(Long tenantId);
    Optional<LocalUso> findByNomeAndTenantId(String nome, Long tenantId);
}