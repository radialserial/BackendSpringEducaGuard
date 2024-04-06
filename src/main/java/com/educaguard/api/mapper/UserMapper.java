package com.educaguard.api.mapper;

import com.educaguard.api.dto.user.UserInputDTO;
import com.educaguard.api.dto.user.UserOutputDTO;
import com.educaguard.domain.models.User;
import org.modelmapper.ModelMapper;

public class UserMapper {

    public static UserOutputDTO mapperUserToUserOutputDTO(User user) {
        ModelMapper mapper = new ModelMapper();
        return mapper.map(user, UserOutputDTO.class);
    }

    public static User mapperUserInputDTOToUser(UserInputDTO userInputDTO) {
        ModelMapper mapper = new ModelMapper();
        return mapper.map(userInputDTO, User.class);
    }

}
