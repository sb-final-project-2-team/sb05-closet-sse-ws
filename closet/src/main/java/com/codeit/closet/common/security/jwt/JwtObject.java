package com.codeit.closet.common.security.jwt;

import com.codeit.closet.common.security.user.UserDTO;

import java.time.Instant;

public record JwtObject(
        Instant issueTime,
        Instant expirationTime,
        UserDTO userDTO,
        String token
) {
    public boolean isExpired() {
        return expirationTime.isBefore(Instant.now());
    }
}
