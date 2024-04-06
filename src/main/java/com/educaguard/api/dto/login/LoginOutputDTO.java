package com.educaguard.api.dto.login;

import com.educaguard.domain.enums.Roles;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginOutputDTO {

    @NotNull
    private Long idUser;
    @NotBlank
    private String token;
    @NotNull
    private Roles role;

}
