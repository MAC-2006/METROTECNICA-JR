package com.metrotecnica.api.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

@Entity
@Table(name = "historico_calibracoes")
public class HistoricoCalibracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id", nullable = false)
    @JsonIgnore
    private Instrumento instrumento;

    @Column(name = "data_certificacao")
    private LocalDate dataCertificacao;

    @Column(length = 50)
    private String certificado;

    private Double erro;
    private Double incerteza;

    @Column(name = "status_na_epoca", length = 50)
    private String statusNaEpoca;

    @Column(name = "pdf_fisico", length = 100)
    private String pdfFisico;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instrumento getInstrumento() { return instrumento; }
    public void setInstrumento(Instrumento instrumento) { this.instrumento = instrumento; }

    public LocalDate getDataCertificacao() { return dataCertificacao; }
    public void setDataCertificacao(LocalDate dataCertificacao) { this.dataCertificacao = dataCertificacao; }

    public String getCertificado() { return certificado; }
    public void setCertificado(String certificado) { this.certificado = certificado; }

    public Double getErro() { return erro; }
    public void setErro(Double erro) { this.erro = erro; }

    public Double getIncerteza() { return incerteza; }
    public void setIncerteza(Double incerteza) { this.incerteza = incerteza; }

    public String getStatusNaEpoca() { return statusNaEpoca; }
    public void setStatusNaEpoca(String statusNaEpoca) { this.statusNaEpoca = statusNaEpoca; }

    public String getPdfFisico() { return pdfFisico; }
    public void setPdfFisico(String pdfFisico) { this.pdfFisico = pdfFisico; }
}