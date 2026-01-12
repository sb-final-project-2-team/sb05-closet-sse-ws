package com.codeit.closet.module.ws.controller;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.ws.config.DirectMessageApiClient;
import com.codeit.closet.module.ws.dto.DirectMessageCreateRequest;
import com.codeit.closet.module.ws.dto.DirectMessageDTO;
import com.codeit.closet.module.ws.dto.DirectMessageSaveRequest;
import com.codeit.closet.module.ws.util.DmKeyUtil;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;


@Slf4j
@Controller
@RequiredArgsConstructor
public class DirectMessageWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DirectMessageApiClient directMessageApiClient;

    @MessageMapping("/direct-messages_send")
    public void send(
            @Payload DirectMessageCreateRequest request,
            Principal principal,
            @Header("simpSessionAttributes") Map<String, Object> sessionAttrs
    ) {
        log.info("[ws-controller] ws서버 저장api 호출 시작");

        UUID senderId = extractSenderId(principal);

        UUID receiverId = request.receiverId();
        String dmKey = DmKeyUtil.of(senderId, receiverId);

        DirectMessageSaveRequest saveReq = new DirectMessageSaveRequest(
                receiverId,
                request.content()
        );

        // 세션에 저장한 토큰 사용
        String token = (String) sessionAttrs.get("ACCESS_TOKEN");
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("MISSING_ACCESS_TOKEN_IN_WS_SESSION");
        }

        DirectMessageDTO saved = directMessageApiClient.save(token, saveReq);

        String destination = "/sub/direct-messages_" + dmKey;
        messagingTemplate.convertAndSend(destination, saved);

        log.info("[ws-controller] ws서버 저장api 호출 완료");
    }

    private UUID extractSenderId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken authentication
                && authentication.getPrincipal() instanceof ClosetUserDetails userDetails) {

            return userDetails.getUserDTO().id();
        }
        throw new RuntimeException("INVALID_WS_PRINCIPAL");
    }
}
