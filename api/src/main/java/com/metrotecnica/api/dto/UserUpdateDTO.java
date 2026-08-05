package com.metrotecnica.api.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    private String nomeCompleto;

    private String role;

    private Boolean canSign;

    // Opcional: só altera a senha se este campo vier preenchido.
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String password;
}
