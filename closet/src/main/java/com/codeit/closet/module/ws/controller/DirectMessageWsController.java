package com.codeit.closet.module.ws.controller;

import com.codeit.closet.module.ws.config.DirectMessageApiClient;
import com.codeit.closet.module.ws.dto.DirectMessageCreateRequest;
import com.codeit.closet.module.ws.dto.DirectMessageDTO;
import com.codeit.closet.module.ws.dto.DirectMessageSaveRequest;
import com.codeit.closet.module.ws.util.DmKeyUtil;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestPart;


@Slf4j
@Controller
@RequiredArgsConstructor
public class DirectMessageWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DirectMessageApiClient directMessageApiClient;

    @MessageMapping("/direct-messages_send")
    public void send(
            @RequestPart DirectMessageCreateRequest request
    ) {

        UUID senderId = request.senderId();
        UUID receiverId = request.receiverId();

        String dmKey = DmKeyUtil.of(senderId, receiverId);

        DirectMessageSaveRequest saveReq = new DirectMessageSaveRequest(
                receiverId,
                request.content()
        );

        DirectMessageDTO saved = directMessageApiClient.save(saveReq);

        String destination = "/sub/direct-messages_" + dmKey;
        messagingTemplate.convertAndSend(destination, saved);
    }
}
