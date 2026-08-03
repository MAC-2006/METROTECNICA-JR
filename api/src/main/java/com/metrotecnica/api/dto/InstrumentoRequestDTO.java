package com.metrotecnica.api.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class InstrumentoRequestDTO {
    private String numeroSequencial;
    private String identificacao;
    private String descricao;
    private String marca;
    private String modelo;
    private String capacidade;
    private String precisao;
    private Long setorId;
    private Long localId;
    private Integer frequenciaMeses;
    private LocalDate dataCertificacao;
    private String certificado;
    private String statusGeral;
    private String observacoes;

    private Double lpMin;
    private Double lpMax;
    private Double aferidoLp;
    private Double lnpMin;
    private Double lnpMax;
    private Double aferidoLnp;
    private Double erro;
    private Double incertezaMedicao;
    private Double criterioAceitacao;

    private List<PontoCalibracaoDTO> pontos;
    private List<HistoricoRetroativoDTO> historicosRetroativos;
}