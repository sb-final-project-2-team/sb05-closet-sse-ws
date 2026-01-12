package com.codeit.closet.common.security.jwt;


import com.codeit.closet.common.security.ClosetUserDetails;

import java.util.Map;
import java.util.UUID;

import com.codeit.closet.common.security.user.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final RoleHierarchy roleHierarchy;
    private final JwtRegistry<UUID> jwtRegistry;

    // Redis Token 상태 정보 생성
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class
        );

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            String token = resolveToken(accessor).orElseThrow(() -> new RuntimeException("INVALID_TOKEN"));

            if (tokenProvider.validateAccessToken(token)
                    && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {

                UserDTO userDTO = tokenProvider.parseAccessToken(token).userDTO();

                ClosetUserDetails userDetails = new ClosetUserDetails(userDTO, null);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                roleHierarchy.getReachableGrantedAuthorities(
                                        userDetails.getAuthorities()
                                )
                        );

                accessor.setUser(authentication);

                // WS 세션에 토큰 저장: WS → API 호출할 때 사용
                Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
                if (sessionAttrs != null) {
                    sessionAttrs.put("ACCESS_TOKEN", token);
                }
            } else {
                throw new RuntimeException("INVALID_TOKEN");
            }
        }

        return message;
    }

    // Redis Token 상태 정보 조회
    private Optional<String> resolveToken(StompHeaderAccessor accessor) {
        String prefix = "Bearer ";

        String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        log.info("test authHeader: {}", StringUtils.hasText(authHeader) && authHeader.startsWith(prefix));
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(prefix)) {
            return Optional.of(authHeader.substring(prefix.length()));
        }

        return Optional.empty();
    }
}
