package com.metrotecnica.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class InstrumentoPageResponseDTO {
    private List<InstrumentoListItemDTO> instrumentos;
    private int totalPaginas;
    private int paginaAtual;
}