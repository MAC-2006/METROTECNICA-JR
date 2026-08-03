package com.metrotecnica.api.repository;

import com.metrotecnica.api.model.HistoricoCalibracao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HistoricoCalibracaoRepository extends JpaRepository<HistoricoCalibracao, Long> {
    Optional<HistoricoCalibracao> findByInstrumentoIdAndCertificado(Long instrumentoId, String certificado);
}