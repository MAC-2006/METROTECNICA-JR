package com.metrotecnica.api.dto;

import com.metrotecnica.api.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String email;
    private String nomeCompleto;
    private String role;
    private Boolean canSign;
    private Long tenantId;

    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getNomeCompleto(),
                user.getRole(),
                user.getCanSign(),
                user.getTenant() != null ? user.getTenant().getId() : null
        );
    }
}
