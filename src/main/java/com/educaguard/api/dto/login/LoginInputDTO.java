package com.educaguard.api.dto.login;


import com.educaguard.utils.Field;
import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginInputDTO {

    @NotBlank(message = Field.USERNAME_MESSAGE)
    private String username;

    @NotBlank(message = Field.PASSWORD_MESSAGE)
    @Size(min = 6, message = Field.PASSWORD_SIZE_MESSAGE)
    private String password;

}
