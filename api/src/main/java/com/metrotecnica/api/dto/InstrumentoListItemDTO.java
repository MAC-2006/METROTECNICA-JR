package com.metrotecnica.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class InstrumentoListItemDTO {
    private Long id;
    private String numeroSequencial;
    private String identificacao;
    private String descricao;
    private LocalDate dataCertificacao;
    private String proxCalibFormatada;
    private String statusGeral;
    private boolean assinado;
    private boolean temPdfFisico;
}