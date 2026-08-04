package com.metrotecnica.api.service;

import com.metrotecnica.api.dto.*;
import com.metrotecnica.api.model.*;
import com.metrotecnica.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstrumentoService {

    private static final List<String> STATUS_MANUAIS = List.of(
            "EXTRAVIADO", "FORA DE USO", "MANUTENÇÃO", "SUCATEADO", "NÃO REQUER CALIBR.", "APROVADO RESTRIÇÃO"
    );

    private final InstrumentoRepository instrumentoRepository;
    private final SetorRepository setorRepository;
    private final LocalUsoRepository localUsoRepository;
    private final TenantRepository tenantRepository;
    private final PontoCalibracaoRepository pontoCalibracaoRepository;

    // ==========================================================
    // CÁLCULO METROLÓGICO (equivalente a calcular_metrologia_vfp_parity)
    // ==========================================================
    public Instrumento calcularMetrologia(Instrumento inst, boolean forcarRecalculo) {
        if (inst.getDataCertificacao() != null && inst.getFrequenciaMeses() != null) {
            if (inst.getDataProximaCalibracao() == null || forcarRecalculo) {
                inst.setDataProximaCalibracao(
                        inst.getDataCertificacao().plusMonths(inst.getFrequenciaMeses())
                );
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM / yyyy", new Locale("pt", "BR"));
            inst.setProxCalibFormatada(inst.getDataProximaCalibracao().format(fmt));
        }

        String statusLp = "APROVADO";
        if (inst.getAferidoLp() != null && inst.getAferidoLp() != 0) {
            if (inst.getLpMin() != null && inst.getLpMax() != null
                    && inst.getAferidoLp() >= inst.getLpMin() && inst.getAferidoLp() <= inst.getLpMax()) {
                inst.setSituacaoLp("APROVADO");
            } else {
                inst.setSituacaoLp("REPROVADO");
                statusLp = "REPROVADO";
            }
        } else {
            inst.setSituacaoLp("");
            statusLp = "PENDENTE";
        }

        String statusLnp = "APROVADO";
        if (inst.getAferidoLnp() != null && inst.getAferidoLnp() != 0) {
            if (inst.getLnpMin() != null && inst.getLnpMax() != null
                    && inst.getAferidoLnp() >= inst.getLnpMin() && inst.getAferidoLnp() <= inst.getLnpMax()) {
                inst.setSituacaoLnp("APROVADO");
            } else {
                inst.setSituacaoLnp("REPROVADO");
                statusLnp = "REPROVADO";
            }
        } else {
            inst.setSituacaoLnp("");
            statusLnp = "PENDENTE";
        }

        String statusMafra = "APROVADO";
        if (inst.getErro() != null) {
            inst.setErroQuadratico(inst.getErro() * inst.getErro());
        }
        if (inst.getIncertezaMedicao() != null) {
            inst.setIncertezaQuadratica(inst.getIncertezaMedicao() * inst.getIncertezaMedicao());
        }
        if (inst.getErro() != null && inst.getIncertezaMedicao() != null) {
            inst.setSomaQuadratica(inst.getErro() + inst.getIncertezaMedicao());
            if (inst.getCriterioAceitacao() != null && inst.getCriterioAceitacao() > 0) {
                if (inst.getSomaQuadratica() <= inst.getCriterioAceitacao()) {
                    inst.setResultadoMafra("APROVADO");
                } else {
                    inst.setResultadoMafra("REPROVADO");
                    statusMafra = "REPROVADO";
                }
            }
        }

        if (inst.getStatusGeral() == null || !STATUS_MANUAIS.contains(inst.getStatusGeral())) {
            boolean semErro = inst.getErro() == null || inst.getErro() == 0;
            if (statusLp.equals("PENDENTE") && statusLnp.equals("PENDENTE") && semErro) {
                inst.setStatusGeral("PENDENTE");
            } else if (statusLp.equals("REPROVADO") || statusLnp.equals("REPROVADO") || statusMafra.equals("REPROVADO")) {
                inst.setStatusGeral("REPROVADO");
            } else {
                inst.setStatusGeral("APROVADO");
            }
        }

        return inst;
    }

    // ==========================================================
    // STATS (dashboard)
    // ==========================================================
    public StatsResponseDTO obterEstatisticas(Long tenantId) {
        LocalDate hoje = LocalDate.now();
        LocalDate alerta = hoje.plusDays(30);

        long vencidos = instrumentoRepository.countByTenantIdAndDataProximaCalibracaoLessThan(tenantId, hoje);
        long emAlerta = instrumentoRepository.countByTenantIdAndDataProximaCalibracaoBetween(tenantId, hoje, alerta);
        long total = instrumentoRepository.countByTenantId(tenantId);

        return new StatsResponseDTO(vencidos, emAlerta, total);
    }

    // ==========================================================
    // LISTAGEM PAGINADA COM FILTROS
    // ==========================================================
    public InstrumentoPageResponseDTO listar(Long tenantId, Integer year, Integer month, String search, int page) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0), 50,
                Sort.by(Sort.Direction.DESC, "numeroSequencial")
        );

        Page<Instrumento> resultado = instrumentoRepository.buscarComFiltros(tenantId, search, year, month, pageable);

        List<InstrumentoListItemDTO> itens = resultado.getContent().stream()
                .map(i -> new InstrumentoListItemDTO(
                        i.getId(),
                        i.getNumeroSequencial() != null ? i.getNumeroSequencial() : "---",
                        i.getIdentificacao(),
                        i.getDescricao(),
                        i.getDataCertificacao(),
                        i.getProxCalibFormatada() != null ? i.getProxCalibFormatada() : "---",
                        i.getStatusGeral() != null ? i.getStatusGeral() : "PENDENTE",
                        i.getDocumentHash() != null,
                        i.getPdfFisico() != null
                ))
                .collect(Collectors.toList());

        return new InstrumentoPageResponseDTO(itens, resultado.getTotalPages(), page);
    }

    // ==========================================================
    // BUSCAR POR ID
    // ==========================================================
    public Instrumento buscarPorId(Long id, Long tenantId) {
        return instrumentoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Instrumento não encontrado"));
    }

    // ==========================================================
    // CRIAR
    // ==========================================================
    @Transactional
    public Instrumento criar(InstrumentoRequestDTO dto, Long tenantId) {
        Instrumento inst = new Instrumento();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));
        inst.setTenant(tenant);

        aplicarCamposBasicos(inst, dto);
        inst.setDataCadastro(LocalDate.now());

        calcularMetrologia(inst, true);
        Instrumento salvo = instrumentoRepository.save(inst);

        aplicarPontos(salvo, dto.getPontos());
        aplicarHistoricosRetroativos(salvo, dto.getHistoricosRetroativos());

        return instrumentoRepository.save(salvo);
    }

    // ==========================================================
    // ATUALIZAR
    // ==========================================================
    @Transactional
    public Instrumento atualizar(Long id, InstrumentoRequestDTO dto, Long tenantId) {
        Instrumento inst = buscarPorId(id, tenantId);

        aplicarCamposBasicos(inst, dto);

        if (dto.getPontos() != null) {
            pontoCalibracaoRepository.deleteByInstrumentoId(inst.getId());
            inst.getPontos().clear();
            aplicarPontos(inst, dto.getPontos());
        }

        if (dto.getHistoricosRetroativos() != null) {
            aplicarHistoricosRetroativos(inst, dto.getHistoricosRetroativos());
        }

        calcularMetrologia(inst, true);
        return instrumentoRepository.save(inst);
    }

    // ==========================================================
    // Helpers privados
    // ==========================================================
    private void aplicarCamposBasicos(Instrumento inst, InstrumentoRequestDTO dto) {
        inst.setNumeroSequencial(dto.getNumeroSequencial());
        inst.setIdentificacao(upper(dto.getIdentificacao()));
        inst.setDescricao(upper(dto.getDescricao()));
        inst.setMarca(upper(dto.getMarca()));
        inst.setModelo(upper(dto.getModelo()));
        inst.setCapacidade(dto.getCapacidade());
        inst.setPrecisao(dto.getPrecisao());
        inst.setFrequenciaMeses(dto.getFrequenciaMeses() != null ? dto.getFrequenciaMeses() : 12);
        inst.setDataCertificacao(dto.getDataCertificacao());
        inst.setCertificado(dto.getCertificado());
        inst.setStatusGeral(dto.getStatusGeral());
        inst.setObservacoes(upper(dto.getObservacoes()));

        inst.setLpMin(dto.getLpMin());
        inst.setLpMax(dto.getLpMax());
        inst.setAferidoLp(dto.getAferidoLp());
        inst.setLnpMin(dto.getLnpMin());
        inst.setLnpMax(dto.getLnpMax());
        inst.setAferidoLnp(dto.getAferidoLnp());
        inst.setErro(dto.getErro());
        inst.setIncertezaMedicao(dto.getIncertezaMedicao());
        inst.setCriterioAceitacao(dto.getCriterioAceitacao());

        if (dto.getSetorId() != null) {
            Setor setor = setorRepository.findById(dto.getSetorId()).orElse(null);
            inst.setSetor(setor);
        } else {
            inst.setSetor(null);
        }

        if (dto.getLocalId() != null) {
            LocalUso local = localUsoRepository.findById(dto.getLocalId()).orElse(null);
            inst.setLocalUso(local);
        } else {
            inst.setLocalUso(null);
        }
    }

    private void aplicarPontos(Instrumento inst, List<PontoCalibracaoDTO> pontosDto) {
        if (pontosDto == null) return;
        int ordem = 0;
        for (PontoCalibracaoDTO p : pontosDto) {
            PontoCalibracao ponto = new PontoCalibracao();
            ponto.setInstrumento(inst);
            ponto.setPontoNominal(p.getPontoNominal());
            ponto.setvIndicado1(p.getVIndicado1());
            ponto.setvIndicado2(p.getVIndicado2());
            ponto.setOrdem(ordem++);
            inst.getPontos().add(ponto);
        }
    }

    private void aplicarHistoricosRetroativos(Instrumento inst, List<HistoricoRetroativoDTO> historicos) {
        if (historicos == null) return;
        for (HistoricoRetroativoDTO h : historicos) {
            if (h.getCertificado() == null || h.getCertificado().isBlank()) continue;

            boolean jaExiste = inst.getHistoricos().stream()
                    .anyMatch(existing -> h.getCertificado().equalsIgnoreCase(existing.getCertificado()));
            if (jaExiste) continue;

            HistoricoCalibracao novoH = new HistoricoCalibracao();
            novoH.setInstrumento(inst);
            novoH.setCertificado(h.getCertificado().trim().toUpperCase());
            novoH.setDataCertificacao(h.getDataCertificacao());
            novoH.setStatusNaEpoca("APROVADO");
            inst.getHistoricos().add(novoH);
        }
    }

    private String upper(String s) {
        return s != null ? s.trim().toUpperCase() : null;
    }

    public Instrumento salvar(Instrumento inst) {
        return instrumentoRepository.save(inst);
    }    
}