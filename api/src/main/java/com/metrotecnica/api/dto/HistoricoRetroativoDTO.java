package com.metrotecnica.api.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class HistoricoRetroativoDTO {
    private Long id;
    private String certificado;
    private LocalDate dataCertificacao;
}