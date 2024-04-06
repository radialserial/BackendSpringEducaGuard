package com.educaguard.api.dto.password;

import com.educaguard.utils.Field;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class NewPasswordInputDTO {

    @NotBlank(message = Field.TOKEN_MESSAGE)
    private String token;

    @NotBlank(message = Field.PASSWORD_MESSAGE)
    @Size(min = 6, message = Field.PASSWORD_SIZE_MESSAGE)
    private String newpassword;

}
