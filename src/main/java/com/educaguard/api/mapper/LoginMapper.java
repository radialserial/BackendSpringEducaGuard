package com.educaguard.api.mapper;

import com.educaguard.api.dto.login.LoginInputDTO;
import com.educaguard.api.dto.login.LoginOutputDTO;
import com.educaguard.domain.models.User;
import org.modelmapper.ModelMapper;

public class LoginMapper
{

    public static User mapperLoginInputDTOToUser(LoginInputDTO loginInputDTO)
    {
        ModelMapper mapper = new ModelMapper();
        return mapper.map(loginInputDTO, User.class);
    }
    public static LoginOutputDTO mapperUserToLoginOutputDTO(User user){
        ModelMapper mapper = new ModelMapper();
        return mapper.map(user,LoginOutputDTO.class);
    }
}
