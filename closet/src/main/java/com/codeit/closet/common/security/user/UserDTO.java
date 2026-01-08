package com.codeit.closet.common.security.user;

import com.codeit.closet.common.entity.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserDTO(
        UUID id,
        Instant createdAt,
        String email,
        String name,
        UserRole role,
        Boolean locked
) {
}