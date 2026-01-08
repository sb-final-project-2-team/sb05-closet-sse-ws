package com.codeit.closet.common.security.user;

import com.codeit.closet.common.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role")
    UserDTO toUserDTO(User user);
}
