package com.metrotecnica.api.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "instrumentos")
public class Instrumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "numero_sequencial", length = 20)
    private String numeroSequencial;

    @Column(nullable = false, length = 50)
    private String identificacao;

    @Column(nullable = false, length = 150)
    private String descricao;

    private String marca;
    private String modelo;
    private String capacidade;
    private String precisao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id")
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id")
    private LocalUso localUso;

    @Column(name = "frequencia_meses")
    private Integer frequenciaMeses = 12;

    @Column(name = "data_cadastro")
    private LocalDate dataCadastro;

    @Column(name = "data_certificacao")
    private LocalDate dataCertificacao;

    @Column(length = 50)
    private String certificado;

    @Column(name = "data_proxima_calibracao")
    private LocalDate dataProximaCalibracao;

    // Lado Passa
    @Column(name = "lp_min")
    private Double lpMin;
    @Column(name = "lp_max")
    private Double lpMax;
    @Column(name = "aferido_lp")
    private Double aferidoLp;
    @Column(name = "situacao_lp", length = 50)
    private String situacaoLp;

    // Lado Não Passa
    @Column(name = "lnp_min")
    private Double lnpMin;
    @Column(name = "lnp_max")
    private Double lnpMax;
    @Column(name = "aferido_lnp")
    private Double aferidoLnp;
    @Column(name = "situacao_lnp", length = 50)
    private String situacaoLnp;

    // Metrologia / Cálculos
    private Double erro;
    @Column(name = "incerteza_medicao")
    private Double incertezaMedicao;
    @Column(name = "criterio_aceitacao")
    private Double criterioAceitacao;
    @Column(name = "erro_quadratico")
    private Double erroQuadratico;
    @Column(name = "incerteza_quadratica")
    private Double incertezaQuadratica;
    @Column(name = "soma_quadratica")
    private Double somaQuadratica;
    @Column(name = "resultado_mafra", length = 50)
    private String resultadoMafra;
    @Column(name = "prox_calib_formatada", length = 20)
    private String proxCalibFormatada;

    @Column(name = "status_geral", length = 50)
    private String statusGeral;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // Assinatura eletrônica
    @Column(name = "document_hash", length = 64)
    private String documentHash;
    @Column(name = "assinatura_data")
    private LocalDateTime assinaturaData;
    @Column(name = "assinante_nome", length = 100)
    private String assinanteNome;
    @Column(name = "assinante_ip", length = 45)
    private String assinanteIp;

    @Column(name = "metrologista_nome", length = 100)
    private String metrologistaNome;
    @Column(name = "metrologista_data")
    private LocalDateTime metrologistaData;
    @Column(name = "metrologista_sig", length = 100)
    private String metrologistaSig;

    @Column(name = "responsavel_nome", length = 100)
    private String responsavelNome;
    @Column(name = "responsavel_data")
    private LocalDateTime responsavelData;
    @Column(name = "responsavel_sig", length = 100)
    private String responsavelSig;

    @Column(name = "layout_pdf", length = 30)
    private String layoutPdf = "padrao";

    @Column(name = "pdf_fisico", length = 100)
    private String pdfFisico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id")
    private Procedimento procedimento;

    @OneToMany(mappedBy = "instrumento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<PontoCalibracao> pontos = new ArrayList<>();

    @OneToMany(mappedBy = "instrumento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoricoCalibracao> historicos = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "instrumento_padrao",
        joinColumns = @JoinColumn(name = "instrumento_id"),
        inverseJoinColumns = @JoinColumn(name = "padrao_id")
    )
    private List<PadraoMetrologico> padroesUtilizados = new ArrayList<>();

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public String getNumeroSequencial() { return numeroSequencial; }
    public void setNumeroSequencial(String numeroSequencial) { this.numeroSequencial = numeroSequencial; }

    public String getIdentificacao() { return identificacao; }
    public void setIdentificacao(String identificacao) { this.identificacao = identificacao; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getCapacidade() { return capacidade; }
    public void setCapacidade(String capacidade) { this.capacidade = capacidade; }

    public String getPrecisao() { return precisao; }
    public void setPrecisao(String precisao) { this.precisao = precisao; }

    public Setor getSetor() { return setor; }
    public void setSetor(Setor setor) { this.setor = setor; }

    public LocalUso getLocalUso() { return localUso; }
    public void setLocalUso(LocalUso localUso) { this.localUso = localUso; }

    public Integer getFrequenciaMeses() { return frequenciaMeses; }
    public void setFrequenciaMeses(Integer frequenciaMeses) { this.frequenciaMeses = frequenciaMeses; }

    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) { this.dataCadastro = dataCadastro; }

    public LocalDate getDataCertificacao() { return dataCertificacao; }
    public void setDataCertificacao(LocalDate dataCertificacao) { this.dataCertificacao = dataCertificacao; }

    public String getCertificado() { return certificado; }
    public void setCertificado(String certificado) { this.certificado = certificado; }

    public LocalDate getDataProximaCalibracao() { return dataProximaCalibracao; }
    public void setDataProximaCalibracao(LocalDate dataProximaCalibracao) { this.dataProximaCalibracao = dataProximaCalibracao; }

    public Double getLpMin() { return lpMin; }
    public void setLpMin(Double lpMin) { this.lpMin = lpMin; }

    public Double getLpMax() { return lpMax; }
    public void setLpMax(Double lpMax) { this.lpMax = lpMax; }

    public Double getAferidoLp() { return aferidoLp; }
    public void setAferidoLp(Double aferidoLp) { this.aferidoLp = aferidoLp; }

    public String getSituacaoLp() { return situacaoLp; }
    public void setSituacaoLp(String situacaoLp) { this.situacaoLp = situacaoLp; }

    public Double getLnpMin() { return lnpMin; }
    public void setLnpMin(Double lnpMin) { this.lnpMin = lnpMin; }

    public Double getLnpMax() { return lnpMax; }
    public void setLnpMax(Double lnpMax) { this.lnpMax = lnpMax; }

    public Double getAferidoLnp() { return aferidoLnp; }
    public void setAferidoLnp(Double aferidoLnp) { this.aferidoLnp = aferidoLnp; }

    public String getSituacaoLnp() { return situacaoLnp; }
    public void setSituacaoLnp(String situacaoLnp) { this.situacaoLnp = situacaoLnp; }

    public Double getErro() { return erro; }
    public void setErro(Double erro) { this.erro = erro; }

    public Double getIncertezaMedicao() { return incertezaMedicao; }
    public void setIncertezaMedicao(Double incertezaMedicao) { this.incertezaMedicao = incertezaMedicao; }

    public Double getCriterioAceitacao() { return criterioAceitacao; }
    public void setCriterioAceitacao(Double criterioAceitacao) { this.criterioAceitacao = criterioAceitacao; }

    public Double getErroQuadratico() { return erroQuadratico; }
    public void setErroQuadratico(Double erroQuadratico) { this.erroQuadratico = erroQuadratico; }

    public Double getIncertezaQuadratica() { return incertezaQuadratica; }
    public void setIncertezaQuadratica(Double incertezaQuadratica) { this.incertezaQuadratica = incertezaQuadratica; }

    public Double getSomaQuadratica() { return somaQuadratica; }
    public void setSomaQuadratica(Double somaQuadratica) { this.somaQuadratica = somaQuadratica; }

    public String getResultadoMafra() { return resultadoMafra; }
    public void setResultadoMafra(String resultadoMafra) { this.resultadoMafra = resultadoMafra; }

    public String getProxCalibFormatada() { return proxCalibFormatada; }
    public void setProxCalibFormatada(String proxCalibFormatada) { this.proxCalibFormatada = proxCalibFormatada; }

    public String getStatusGeral() { return statusGeral; }
    public void setStatusGeral(String statusGeral) { this.statusGeral = statusGeral; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getDocumentHash() { return documentHash; }
    public void setDocumentHash(String documentHash) { this.documentHash = documentHash; }

    public LocalDateTime getAssinaturaData() { return assinaturaData; }
    public void setAssinaturaData(LocalDateTime assinaturaData) { this.assinaturaData = assinaturaData; }

    public String getAssinanteNome() { return assinanteNome; }
    public void setAssinanteNome(String assinanteNome) { this.assinanteNome = assinanteNome; }

    public String getAssinanteIp() { return assinanteIp; }
    public void setAssinanteIp(String assinanteIp) { this.assinanteIp = assinanteIp; }

    public String getMetrologistaNome() { return metrologistaNome; }
    public void setMetrologistaNome(String metrologistaNome) { this.metrologistaNome = metrologistaNome; }

    public LocalDateTime getMetrologistaData() { return metrologistaData; }
    public void setMetrologistaData(LocalDateTime metrologistaData) { this.metrologistaData = metrologistaData; }

    public String getMetrologistaSig() { return metrologistaSig; }
    public void setMetrologistaSig(String metrologistaSig) { this.metrologistaSig = metrologistaSig; }

    public String getResponsavelNome() { return responsavelNome; }
    public void setResponsavelNome(String responsavelNome) { this.responsavelNome = responsavelNome; }

    public LocalDateTime getResponsavelData() { return responsavelData; }
    public void setResponsavelData(LocalDateTime responsavelData) { this.responsavelData = responsavelData; }

    public String getResponsavelSig() { return responsavelSig; }
    public void setResponsavelSig(String responsavelSig) { this.responsavelSig = responsavelSig; }

    public String getLayoutPdf() { return layoutPdf; }
    public void setLayoutPdf(String layoutPdf) { this.layoutPdf = layoutPdf; }

    public String getPdfFisico() { return pdfFisico; }
    public void setPdfFisico(String pdfFisico) { this.pdfFisico = pdfFisico; }

    public Procedimento getProcedimento() { return procedimento; }
    public void setProcedimento(Procedimento procedimento) { this.procedimento = procedimento; }

    public List<PontoCalibracao> getPontos() { return pontos; }
    public void setPontos(List<PontoCalibracao> pontos) { this.pontos = pontos; }

    public List<HistoricoCalibracao> getHistoricos() { return historicos; }
    public void setHistoricos(List<HistoricoCalibracao> historicos) { this.historicos = historicos; }

    public List<PadraoMetrologico> getPadroesUtilizados() { return padroesUtilizados; }
    public void setPadroesUtilizados(List<PadraoMetrologico> padroesUtilizados) { this.padroesUtilizados = padroesUtilizados; }
}