package com.metrotecnica.api.controller;

import com.metrotecnica.api.dto.InstrumentoPageResponseDTO;
import com.metrotecnica.api.dto.InstrumentoRequestDTO;
import com.metrotecnica.api.dto.StatsResponseDTO;
import com.metrotecnica.api.model.HistoricoCalibracao;
import com.metrotecnica.api.model.Instrumento;
import com.metrotecnica.api.model.PontoCalibracao;
import com.metrotecnica.api.security.UserPrincipal;
import com.metrotecnica.api.service.InstrumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InstrumentoController {

    private final InstrumentoService instrumentoService;

    // ==========================================================
    // GET /api/stats
    // ==========================================================
    @GetMapping("/stats")
    public ResponseEntity<StatsResponseDTO> obterEstatisticas(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(instrumentoService.obterEstatisticas(user.getTenantId()));
    }

    // ==========================================================
    // GET /api/instrumentos (paginado + filtros)
    // ==========================================================
    @GetMapping("/instrumentos")
    public ResponseEntity<InstrumentoPageResponseDTO> listar(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(
                instrumentoService.listar(user.getTenantId(), year, month, search, page)
        );
    }

    // ==========================================================
    // GET /api/instrumentos/{id}
    // ==========================================================
    @GetMapping("/instrumentos/{id}")
    public ResponseEntity<?> obterPorId(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal user) {
        Instrumento inst = instrumentoService.buscarPorId(id, user.getTenantId());
        return ResponseEntity.ok(montarDetalheCompleto(inst));
    }

    // ==========================================================
    // POST /api/instrumentos
    // ==========================================================
    @PostMapping("/instrumentos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criar(
            @Valid @RequestBody InstrumentoRequestDTO dto,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Instrumento novo = instrumentoService.criar(dto, user.getTenantId());
        return ResponseEntity.status(201).body(Map.of("message", "Criado", "id", novo.getId()));
    }

    // ==========================================================
    // PUT /api/instrumentos/{id}
    // ==========================================================
    @PutMapping("/instrumentos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody InstrumentoRequestDTO dto,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        instrumentoService.atualizar(id, dto, user.getTenantId());
        return ResponseEntity.ok(Map.of("message", "Atualizado com sucesso"));
    }

    // ==========================================================
    // Helper: monta o JSON detalhado do instrumento (equivalente ao instrumento_schema.dump + historicos)
    // ==========================================================
    private Map<String, Object> montarDetalheCompleto(Instrumento inst) {
        Map<String, Object> dados = new HashMap<>();

        dados.put("id", inst.getId());
        dados.put("numeroSequencial", inst.getNumeroSequencial());
        dados.put("identificacao", inst.getIdentificacao());
        dados.put("descricao", inst.getDescricao());
        dados.put("marca", inst.getMarca());
        dados.put("modelo", inst.getModelo());
        dados.put("capacidade", inst.getCapacidade());
        dados.put("precisao", inst.getPrecisao());
        dados.put("setorId", inst.getSetor() != null ? inst.getSetor().getId() : null);
        dados.put("localId", inst.getLocalUso() != null ? inst.getLocalUso().getId() : null);
        dados.put("frequenciaMeses", inst.getFrequenciaMeses());
        dados.put("dataCertificacao", inst.getDataCertificacao());
        dados.put("certificado", inst.getCertificado());
        dados.put("dataProximaCalibracao", inst.getDataProximaCalibracao());
        dados.put("proxCalibFormatada", inst.getProxCalibFormatada());
        dados.put("lpMin", inst.getLpMin());
        dados.put("lpMax", inst.getLpMax());
        dados.put("aferidoLp", inst.getAferidoLp());
        dados.put("situacaoLp", inst.getSituacaoLp());
        dados.put("lnpMin", inst.getLnpMin());
        dados.put("lnpMax", inst.getLnpMax());
        dados.put("aferidoLnp", inst.getAferidoLnp());
        dados.put("situacaoLnp", inst.getSituacaoLnp());
        dados.put("erro", inst.getErro());
        dados.put("incertezaMedicao", inst.getIncertezaMedicao());
        dados.put("criterioAceitacao", inst.getCriterioAceitacao());
        dados.put("somaQuadratica", inst.getSomaQuadratica());
        dados.put("resultadoMafra", inst.getResultadoMafra());
        dados.put("statusGeral", inst.getStatusGeral());
        dados.put("observacoes", inst.getObservacoes());

        List<Map<String, Object>> pontos = inst.getPontos().stream()
                .map(this::mapPonto)
                .collect(Collectors.toList());
        dados.put("pontos", pontos);

        List<Map<String, Object>> historicos = inst.getHistoricos().stream()
                .map(this::mapHistorico)
                .collect(Collectors.toList());
        dados.put("historicos", historicos);

        return dados;
    }

    private Map<String, Object> mapPonto(PontoCalibracao p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("pontoNominal", p.getPontoNominal());
        m.put("vIndicado1", p.getvIndicado1());
        m.put("vIndicado2", p.getvIndicado2());
        return m;
    }

    private Map<String, Object> mapHistorico(HistoricoCalibracao h) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", h.getId());
        m.put("data", h.getDataCertificacao() != null
                ? h.getDataCertificacao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "---");
        m.put("certificado", h.getCertificado());
        m.put("status", h.getStatusNaEpoca());
        m.put("temPdfFisico", h.getPdfFisico() != null);
        return m;
    }
}