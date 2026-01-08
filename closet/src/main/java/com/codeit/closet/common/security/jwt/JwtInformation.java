package com.codeit.closet.common.security.jwt;

import com.codeit.closet.common.security.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtInformation {

    private UserDTO userDTO;
    private String accessToken;
    private String refreshToken;

    public void rotate(String newAccessToken, String newRefreshToken) {
        this.refreshToken = newRefreshToken;
        this.accessToken = newAccessToken;
    }
}
