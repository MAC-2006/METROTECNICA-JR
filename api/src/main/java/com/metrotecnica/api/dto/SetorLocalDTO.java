package com.metrotecnica.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetorLocalDTO {
    @NotBlank
    private String nome;
}