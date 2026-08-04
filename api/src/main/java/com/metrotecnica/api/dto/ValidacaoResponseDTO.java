package com.metrotecnica.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ValidacaoResponseDTO {
    private boolean valido;
    private String identificacao;
    private String descricao;
    private String marca;
    private String modelo;
    private String tenantNome;
    private LocalDate dataCertificacao;
    private String proxCalibFormatada;
    private String statusGeral;
    private String metrologistaNome;
    private String responsavelNome;
    private String documentHashParcial;
}