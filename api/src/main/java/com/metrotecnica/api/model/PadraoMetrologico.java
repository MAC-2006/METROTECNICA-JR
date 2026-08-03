package com.metrotecnica.api.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "padroes_metrologicos")
public class PadraoMetrologico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String identificacao;

    @Column(name = "descricao_detalhada", nullable = false, columnDefinition = "TEXT")
    private String descricaoDetalhada;

    @Column(name = "certificado_origem", length = 100)
    private String certificadoOrigem;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    private Boolean ativo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIdentificacao() { return identificacao; }
    public void setIdentificacao(String identificacao) { this.identificacao = identificacao; }

    public String getDescricaoDetalhada() { return descricaoDetalhada; }
    public void setDescricaoDetalhada(String descricaoDetalhada) { this.descricaoDetalhada = descricaoDetalhada; }

    public String getCertificadoOrigem() { return certificadoOrigem; }
    public void setCertificadoOrigem(String certificadoOrigem) { this.certificadoOrigem = certificadoOrigem; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}