package com.metrotecnica.api.repository;

import com.metrotecnica.api.model.Procedimento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProcedimentoRepository extends JpaRepository<Procedimento, Long> {
    Optional<Procedimento> findByCodigo(String codigo);
}