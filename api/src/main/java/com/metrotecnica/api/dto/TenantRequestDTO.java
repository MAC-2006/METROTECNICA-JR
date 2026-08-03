package com.metrotecnica.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantRequestDTO {
    @NotBlank
    private String name;

    private String razaoSocial;
    private String cnpj;
    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String telefone;

    @NotBlank
    private String slug;

    private String url;
}