package com.metrotecnica.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class MigracaoResponseDTO {
    private String message;
    private int count;
    private Long tenantId;
    private String name;
    private String slug;
    private List<String> warnings;
}