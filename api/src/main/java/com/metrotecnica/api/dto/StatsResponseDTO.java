package com.metrotecnica.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatsResponseDTO {
    private long vencidos;
    private long alerta;
    private long total;
}