package com.metrotecnica.api.repository;

import com.metrotecnica.api.model.PontoCalibracao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PontoCalibracaoRepository extends JpaRepository<PontoCalibracao, Long> {
    void deleteByInstrumentoId(Long instrumentoId);
}