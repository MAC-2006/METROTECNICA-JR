package com.metrotecnica.api.repository;

import com.metrotecnica.api.model.PadraoMetrologico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PadraoMetrologicoRepository extends JpaRepository<PadraoMetrologico, Long> {
    Optional<PadraoMetrologico> findByIdentificacao(String identificacao);
}