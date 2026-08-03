package com.metrotecnica.api.repository;

import com.metrotecnica.api.model.Instrumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InstrumentoRepository extends JpaRepository<Instrumento, Long> {

    Optional<Instrumento> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Instrumento> findByIdentificacaoAndTenantId(String identificacao, Long tenantId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndDataProximaCalibracaoLessThan(Long tenantId, LocalDate hoje);

    long countByTenantIdAndDataProximaCalibracaoBetween(Long tenantId, LocalDate inicio, LocalDate fim);

    List<Instrumento> findByDataProximaCalibracaoBetween(LocalDate inicio, LocalDate fim);

    @Query("""
        SELECT i FROM Instrumento i
        WHERE i.tenant.id = :tenantId
        AND (:search IS NULL OR :search = '' OR
             LOWER(i.identificacao) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(i.descricao) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(i.numeroSequencial) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:year IS NULL OR YEAR(i.dataCertificacao) = :year)
        AND (:month IS NULL OR MONTH(i.dataCertificacao) = :month)
        """)
    Page<Instrumento> buscarComFiltros(
            @Param("tenantId") Long tenantId,
            @Param("search") String search,
            @Param("year") Integer year,
            @Param("month") Integer month,
            Pageable pageable
    );

    List<Instrumento> findByTenantIdAndSetorIdOrderByDescricaoAsc(Long tenantId, Long setorId);

    List<Instrumento> findByTenantIdAndStatusGeral(Long tenantId, String statusGeral);

    List<Instrumento> findByTenantIdAndLocalUsoId(Long tenantId, Long localId);

    List<Instrumento> findByTenantIdAndDataProximaCalibracaoLessThan(Long tenantId, LocalDate hoje);

    List<Instrumento> findByTenantIdAndDataProximaCalibracaoBetween(Long tenantId, LocalDate inicio, LocalDate fim);

    List<Instrumento> findByTenantIdOrderById(Long tenantId);

    List<Instrumento> findByTenantIdOrderBySetorIdAscDescricaoAsc(Long tenantId);
}