package com.metrotecnica.api.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "pontos_calibracao")
public class PontoCalibracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id", nullable = false)
    @JsonIgnore
    private Instrumento instrumento;

    @Column(name = "ponto_nominal", length = 20)
    private String pontoNominal;

    @Column(name = "v_indicado_1")
    private Double vIndicado1;

    @Column(name = "v_indicado_2")
    private Double vIndicado2;

    @Column(name = "v_indicado_3")
    private Double vIndicado3;

    @Column(name = "erro_calculado")
    private Double erroCalculado;

    private Integer ordem = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instrumento getInstrumento() { return instrumento; }
    public void setInstrumento(Instrumento instrumento) { this.instrumento = instrumento; }

    public String getPontoNominal() { return pontoNominal; }
    public void setPontoNominal(String pontoNominal) { this.pontoNominal = pontoNominal; }

    public Double getvIndicado1() { return vIndicado1; }
    public void setvIndicado1(Double vIndicado1) { this.vIndicado1 = vIndicado1; }

    public Double getvIndicado2() { return vIndicado2; }
    public void setvIndicado2(Double vIndicado2) { this.vIndicado2 = vIndicado2; }

    public Double getvIndicado3() { return vIndicado3; }
    public void setvIndicado3(Double vIndicado3) { this.vIndicado3 = vIndicado3; }

    public Double getErroCalculado() { return erroCalculado; }
    public void setErroCalculado(Double erroCalculado) { this.erroCalculado = erroCalculado; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
}