package com.codeit.closet.controller;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.common.security.user.UserDTO;
import com.codeit.closet.module.ws.config.DirectMessageApiClient;
import com.codeit.closet.module.ws.controller.DirectMessageWsController;
import com.codeit.closet.module.ws.dto.DirectMessageCreateRequest;
import com.codeit.closet.module.ws.dto.DirectMessageDTO;
import com.codeit.closet.module.ws.dto.DirectMessageSaveRequest;
import com.codeit.closet.module.ws.util.DmKeyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectMessageWsController 단위 테스트")
class DirectMessageWsControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private DirectMessageApiClient directMessageApiClient;

    @InjectMocks
    private DirectMessageWsController controller;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    @Captor
    private ArgumentCaptor<Object> payloadCaptor;

    private UUID senderId;
    private UUID receiverId;

    @BeforeEach
    void setUp() {
        senderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        receiverId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    }

    @Test
    @DisplayName("토큰이 없으면 예외가 발생한다 - MISSING_ACCESS_TOKEN_IN_WS_SESSION")
    void send_Failure_MissingAccessTokenInWsSession() {
        // Given: 요청 DTO (principal 기반 senderId를 사용)
        DirectMessageCreateRequest request =
                new DirectMessageCreateRequest(receiverId, senderId, "hi");

        // Given: principal 정상
        Principal principal = principalWithSenderId(senderId);

        // Given: WS 세션에 토큰 없음
        Map<String, Object> sessionAttrs = new HashMap<>();

        // When & Then
        assertThatThrownBy(() -> controller.send(request, principal, sessionAttrs))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("MISSING_ACCESS_TOKEN_IN_WS_SESSION");

        // Then: 외부 호출/전송이 발생하면 안 됨
        verifyNoInteractions(directMessageApiClient);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("principal이 올바르지 않으면 예외가 발생한다 - INVALID_WS_PRINCIPAL")
    void send_Failure_InvalidWsPrincipal() {
        // Given
        DirectMessageCreateRequest request =
                new DirectMessageCreateRequest(receiverId, senderId, "hi");

        // Given: 잘못된 principal
        Principal invalidPrincipal = () -> "someone";

        // Given: 토큰은 있어도 principal이 틀리면 실패해야 함
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("ACCESS_TOKEN", "test-token");

        // When & Then
        assertThatThrownBy(() -> controller.send(request, invalidPrincipal, sessionAttrs))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("INVALID_WS_PRINCIPAL");

        // Then
        verifyNoInteractions(directMessageApiClient);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("요청 senderId와 principal senderId가 다르면 예외가 발생한다")
    void send_Failure_SenderIdMismatch() {
        // Given
        UUID principalSenderId = UUID.randomUUID();
        UUID requestSenderId = UUID.randomUUID();

        DirectMessageCreateRequest request =
                new DirectMessageCreateRequest(receiverId, requestSenderId, "hi");

        Principal principal = principalWithSenderId(principalSenderId);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("ACCESS_TOKEN", "test-token");

        // When & Then
        assertThatThrownBy(() -> controller.send(request, principal, sessionAttrs))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("SENDER_ID_MISMATCH");

        // Then: 저장 API 호출/전송이 발생하면 안 됨
        verifyNoInteractions(directMessageApiClient);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("정상 요청이면 저장 API를 호출하고 dmKey 경로로 publish 한다")
    void send_Success_CallSaveApi_AndPublish() {
        // Given: 요청 DTO
        DirectMessageCreateRequest request =
                new DirectMessageCreateRequest(receiverId, senderId, "hello");

        // Given: principal 정상
        Principal principal = principalWithSenderId(senderId);

        // Given: WS 세션 토큰
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("ACCESS_TOKEN", "test-token");

        // Given: API 저장 결과
        DirectMessageDTO saved = mock(DirectMessageDTO.class);
        when(directMessageApiClient.save(eq("test-token"), any(DirectMessageSaveRequest.class)))
                .thenReturn(saved);

        // When
        controller.send(request, principal, sessionAttrs);

        // Then: 저장 API 요청 내용(receiverId/content) 검증
        ArgumentCaptor<DirectMessageSaveRequest> saveRequestCaptor =
                ArgumentCaptor.forClass(DirectMessageSaveRequest.class);

        verify(directMessageApiClient, times(1))
                .save(eq("test-token"), saveRequestCaptor.capture());

        DirectMessageSaveRequest captured = saveRequestCaptor.getValue();
        assertThat(captured.receiverId()).isEqualTo(receiverId);
        assertThat(captured.content()).isEqualTo("hello");

        // Then: publish destination/payload 검증
        verify(messagingTemplate, times(1))
                .convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        String expectedDmKey = DmKeyUtil.of(senderId, receiverId);
        String expectedDestination = "/sub/direct-messages_" + expectedDmKey;

        assertThat(destinationCaptor.getValue()).isEqualTo(expectedDestination);
        assertThat(payloadCaptor.getValue()).isSameAs(saved);
    }

    /**
     * senderId를 가진 Principal(UsernamePasswordAuthenticationToken)을 만드는 헬퍼
     */
    private Principal principalWithSenderId(UUID senderId) {
        ClosetUserDetails userDetails = mock(ClosetUserDetails.class);

        UserDTO userDto =
                mock(UserDTO.class);

        when(userDetails.getUserDTO()).thenReturn(userDto);
        when(userDto.id()).thenReturn(senderId);

        return new UsernamePasswordAuthenticationToken(userDetails, null);
    }
}
