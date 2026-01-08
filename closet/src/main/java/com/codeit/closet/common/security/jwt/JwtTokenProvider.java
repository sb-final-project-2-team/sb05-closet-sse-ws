package com.codeit.closet.common.security.jwt;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.common.security.user.UserDTO;
import com.codeit.closet.common.entity.UserRole;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "CLOSET_REFRESH_TOKEN";

    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final String issuer;

    private final JWSSigner accessTokenSigner;
    private final JWSVerifier accessTokenVerifier;
    private final JWSSigner refreshTokenSigner;
    private final JWSVerifier refreshTokenVerifier;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-validity-seconds}") long accessTokenValiditySeconds,
            @Value("${security.jwt.refresh-token-validity-seconds}") long refreshTokenValiditySeconds,
            @Value("${security.jwt.issuer}") String issuer
    ) throws JOSEException {
        this.accessTokenExpirationMs = accessTokenValiditySeconds * 1000L;
        this.refreshTokenExpirationMs = refreshTokenValiditySeconds * 1000L;
        this.issuer = issuer;

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenSigner = new MACSigner(secretBytes);
        this.accessTokenVerifier = new MACVerifier(secretBytes);
        this.refreshTokenSigner = new MACSigner(secretBytes);
        this.refreshTokenVerifier = new MACVerifier(secretBytes);
    }

    // ==========================================
    // ================ 토큰 생성 ================
    // ==========================================
    public String generateAccessToken(ClosetUserDetails closetUserDetails) throws JOSEException {
        return generateToken(closetUserDetails, accessTokenExpirationMs, accessTokenSigner,
                "access_token");
    }

    public String generateRefreshToken(ClosetUserDetails closetUserDetails) throws JOSEException {
        return generateToken(closetUserDetails, refreshTokenExpirationMs, refreshTokenSigner,
                "refresh_token");
    }

    // ==========================================
    // ================ 토큰 검증 ================
    // ==========================================
    public boolean validateAccessToken(String token) {
        return validateToken(token, accessTokenVerifier, "access_token");
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshTokenVerifier, "refresh_token");
    }

    // ==========================================
    // ============ 리프레시 토큰 변환 =============
    // ==========================================
    public Cookie generateRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenExpirationMs / 1000L));
        return cookie;
    }

    public Cookie generateRefreshTokenExpirationCookie() {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
    // ==========================================
    // ================ JWT 파싱 ================
    // ==========================================
    public String getEmailFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public JwtObject parseAccessToken(String token) {
        return parseInternal(token, "access_token");
    }

    public JwtObject parseRefreshToken(String token) {
        return parseInternal(token, "refresh_token");
    }

    // ==========================================
    // ================ JWT 유틸 ================
    // ==========================================
    private String generateToken(
            ClosetUserDetails closetUserDetails, long expirationMs,
            JWSSigner signer, String tokenType) throws JOSEException {
        String tokenId = UUID.randomUUID().toString();
        UserDTO userDTO = closetUserDetails.getUserDTO();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        JWTClaimsSet jwtClaimsSet = new Builder()
                .subject(userDTO.email()) // 유저 식별자
                .jwtID(tokenId) // 토큰 고유 ID
                .issuer(issuer) // 토큰 발급자
                .claim("userId", userDTO.id().toString())
                .claim("type", tokenType) // accessToken 인지 refreshToken 인지
                .claim("name", userDTO.name())
                .claim("roles", closetUserDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .issueTime(now)
                .expirationTime(expiryDate)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaimsSet);

        signedJWT.sign(signer);
        String token = signedJWT.serialize();

        log.debug("Generated {} token for user: {}", tokenType, userDTO.email());
        return token;
    }

    private boolean validateToken(String token, JWSVerifier verifier, String expectedType) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            if (!signedJWT.verify(verifier)){
                log.debug("JWT signature verification failed for {} token", expectedType);
                return false;
            }

            String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
            if (!expectedType.equals(tokenType)) {
                log.debug("JWT token type mismatch: expected {}, got {}", expectedType, tokenType);
                return false;
            }

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                log.debug("JWT {} token expired", expectedType);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.debug("JWT {} token validation failed: {}", expectedType, e.getMessage());
            return false;
        }
    }

    private JwtObject parseInternal(String token, String expectedType) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();

            JWSVerifier verifier = "access_token".equals(expectedType)
                    ? accessTokenVerifier
                    : refreshTokenVerifier;

            if (!signedJWT.verify(verifier)) {
                throw new IllegalArgumentException("JWT 서명 검증 실패");
            }

            String actualType = jwtClaimsSet.getStringClaim("type");
            if (!expectedType.equals(actualType)) {
                throw new IllegalArgumentException("JWT 타입 불일치: expected="
                        + expectedType + ", actual=" + actualType);
            }

            Date expirationTime = jwtClaimsSet.getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                throw new IllegalArgumentException("JWT 만료됨");
            }

            UUID userId = UUID.fromString(jwtClaimsSet.getStringClaim("userId"));
            String email = jwtClaimsSet.getSubject();
            String name = jwtClaimsSet.getStringClaim("name");
            Date issueTime = jwtClaimsSet.getIssueTime();

            List<String> roleList = jwtClaimsSet.getStringListClaim("roles");
            if (roleList == null || roleList.isEmpty()) {
                throw new IllegalArgumentException("JWT에 roles 클레임이 없음.");
            }
            String roleString = roleList.get(0);
            String roleName = roleString.startsWith("ROLE_")
                    ? roleString.substring(5)
                    : roleString;

            UserRole primaryRole = UserRole.valueOf(roleName);

            UserDTO userDTO = new UserDTO(
                    userId,
                    null,
                    email,
                    name,
                    primaryRole,
                    null
            );

            return new JwtObject(
                    issueTime.toInstant(),
                    expirationTime.toInstant(),
                    userDTO,
                    token
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("JWT 파싱 실패: " + e.getMessage(), e);
        }
    }
}
