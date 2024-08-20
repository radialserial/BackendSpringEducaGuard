package com.educaguard.api.dto.user;
import com.educaguard.domain.enums.Roles;
import com.educaguard.utils.Field;
import jakarta.validation.constraints.*;
import lombok.*;

import static com.educaguard.utils.Field.FIRST_LETTER_NAME_MESSAGE;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInputDTO {

    @NotBlank(message = Field.NAME_MESSAGE)
    @Size(min = 4, message = Field.NAME_SIZE_MESSAGE)
    @Pattern(regexp = "^[A-Z]+(.)*", message = FIRST_LETTER_NAME_MESSAGE) // garante que a primeira letra seja maiuscula
    private String name;

    @NotBlank(message = Field.USERNAME_MESSAGE)
    private String username;

    @NotBlank(message = Field.PASSWORD_MESSAGE)
    @Size(min = 6, message = Field.PASSWORD_SIZE_MESSAGE)
    private String password;

    @NotBlank(message = Field.EMAIL_MESSAGE)
    @Email(message = Field.EMAIL_VALID_MESSAGE)
    private String email;

    @NotNull(message = "Informe a role do novo usuário!")
    private Roles role;

}
