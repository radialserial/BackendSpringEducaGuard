package com.educaguard.api.dto.user;

import com.educaguard.domain.enums.Roles;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserOutputDTO {

    @NotNull
    private Long idUser;
    @NotBlank
    @Size(min = 4)
    //@Pattern(regexp = "^[A-Z]+(.)*") // garante que a primeira letra seja maiuscula
    private String name;
    @NotBlank
    private String about;
    @NotBlank
    private String image;
    @NotNull
    private Roles role;

}
