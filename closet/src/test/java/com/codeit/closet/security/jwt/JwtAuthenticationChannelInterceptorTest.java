package com.codeit.closet.security.jwt;

import com.codeit.closet.common.entity.UserRole;
import com.codeit.closet.common.security.jwt.JwtAuthenticationChannelInterceptor;
import com.codeit.closet.common.security.jwt.JwtObject;
import com.codeit.closet.common.security.jwt.JwtRegistry;
import com.codeit.closet.common.security.jwt.JwtTokenProvider;
import com.codeit.closet.common.security.user.UserDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationChannelInterceptor 단위 테스트")
class JwtAuthenticationChannelInterceptorTest {

    @Mock
    JwtTokenProvider tokenProvider;

    @Mock
    RoleHierarchy roleHierarchy;

    @Mock
    JwtRegistry<UUID> jwtRegistry;

    @Mock
    MessageChannel channel;

    @InjectMocks
    JwtAuthenticationChannelInterceptor interceptor;

    @Test
    @DisplayName("CONNECT 요청에서 Authorization 헤더가 없으면 INVALID_TOKEN 예외가 발생한다")
    void preSend_Failure_MissingAuthorizationHeader() {
        // Given: CONNECT accessor 생성
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionAttributes(new HashMap<>());

        Message<byte[]> message = createMessage(accessor);

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("INVALID_TOKEN");

        // Then: 토큰이 없으니 tokenProvider/jwtRegistry는 호출되면 안 됨
        verifyNoInteractions(tokenProvider);
        verifyNoInteractions(jwtRegistry);
    }

    @Test
    @DisplayName("CONNECT 요청에서 토큰이 있지만 validateAccessToken이 false면 INVALID_TOKEN 예외가 발생한다")
    void preSend_Failure_InvalidTokenByValidation() {
        // Given
        String rawToken = "abc.def.ghi";

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionAttributes(new HashMap<>());
        accessor.addNativeHeader(HttpHeaders.AUTHORIZATION, "Bearer " + rawToken);

        Message<byte[]> message = createMessage(accessor);

        when(tokenProvider.validateAccessToken(rawToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("INVALID_TOKEN");

        // Then: validate에서 실패했으므로 registry/parse는 호출되면 안 됨
        verify(jwtRegistry, never()).hasActiveJwtInformationByAccessToken(anyString());
        verify(tokenProvider, never()).parseAccessToken(anyString());
    }

    @Test
    @DisplayName("CONNECT 요청에서 validate는 성공하지만 registry 체크가 false면 INVALID_TOKEN 예외가 발생한다")
    void preSend_Failure_InvalidTokenByRegistry() {
        // Given
        String rawToken = "abc.def.ghi";

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionAttributes(new HashMap<>());
        accessor.addNativeHeader(HttpHeaders.AUTHORIZATION, "Bearer " + rawToken);

        Message<byte[]> message = createMessage(accessor);

        when(tokenProvider.validateAccessToken(rawToken)).thenReturn(true);
        when(jwtRegistry.hasActiveJwtInformationByAccessToken(rawToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("INVALID_TOKEN");

        // Then: registry 실패니까 parse는 호출되면 안 됨
        verify(tokenProvider, never()).parseAccessToken(anyString());
    }

    @Test
    @DisplayName("CONNECT 요청에서 토큰이 유효하면 user가 세팅되고 WS 세션에 ACCESS_TOKEN이 저장된다")
    void preSend_Success_SetAuthenticationAndStoreTokenInSession() {
        // Given
        String rawToken = "abc.def.ghi";

        Map<String, Object> session = new HashMap<>();
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionAttributes(session);
        accessor.addNativeHeader(HttpHeaders.AUTHORIZATION, "Bearer " + rawToken);

        Message<byte[]> message = createMessage(accessor);

        // Given: validate + registry 통과
        when(tokenProvider.validateAccessToken(rawToken)).thenReturn(true);
        when(jwtRegistry.hasActiveJwtInformationByAccessToken(rawToken)).thenReturn(true);

        // Given: ClosetUserDetails.getAuthorities()에서 role 필요
        UserDTO userDTO = mock(UserDTO.class);
        when(userDTO.role()).thenReturn(UserRole.USER);

        JwtObject jwtObject = mock(JwtObject.class);
        when(jwtObject.userDTO()).thenReturn(userDTO);
        when(tokenProvider.parseAccessToken(rawToken)).thenReturn(jwtObject);

        // Given: roleHierarchy는 authorities 가공 → 입력을 그대로 반환하도록
        when(roleHierarchy.getReachableGrantedAuthorities(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // When: 반환된 message로 검증하는 게 안전함(인터셉터가 새 message를 리턴할 수도 있으니까)
        Message<?> result = interceptor.preSend(message, channel);

        // Then
        StompHeaderAccessor after = StompHeaderAccessor.wrap(result);

        assertThat(after.getUser()).isNotNull(); // Authentication 세팅 여부
        assertThat(after.getSessionAttributes()).containsEntry("ACCESS_TOKEN", rawToken); // rawToken 저장 여부
    }

    /**
     * STOMP accessor 기반으로 "mutable 상태"의 메시지를 만든다.
     */
    private Message<byte[]> createMessage(StompHeaderAccessor accessor) {
        accessor.setLeaveMutable(true); // ✅ 핵심: Already immutable 방지
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
