package com.metrotecnica.api.dto;

import lombok.Data;

@Data
public class PontoCalibracaoDTO {
    private Long id;
    private String pontoNominal;
    private Double vIndicado1;
    private Double vIndicado2;
}