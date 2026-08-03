package com.metrotecnica.api.repository;

import com.metrotecnica.api.model.Setor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SetorRepository extends JpaRepository<Setor, Long> {
    List<Setor> findByTenantIdOrderByNomeAsc(Long tenantId);
    Optional<Setor> findByNomeAndTenantId(String nome, Long tenantId);
}