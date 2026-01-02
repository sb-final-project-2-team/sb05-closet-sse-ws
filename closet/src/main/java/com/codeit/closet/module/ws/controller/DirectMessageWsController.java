package com.codeit.closet.module.ws.controller;

import com.codeit.closet.module.ws.config.DirectMessageApiClient;
import com.codeit.closet.module.ws.dto.DirectMessageCreateRequest;
import com.codeit.closet.module.ws.dto.DirectMessageDTO;
import com.codeit.closet.module.ws.dto.DirectMessagePersistRequest;
import com.codeit.closet.module.ws.util.DmKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class DirectMessageWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DirectMessageApiClient directMessageApiClient;

    @MessageMapping("/direct-messages_send")
    public void send(DirectMessageCreateRequest req) {

        String dmKey = DmKeyUtil.of(req.senderId(), req.receiverId());

        DirectMessagePersistRequest persistReq = new DirectMessagePersistRequest(
                req.receiverId(),
                req.senderId(),
                req.content()
        );

        DirectMessageDTO saved = directMessageApiClient.persist(persistReq);

        String destination = "/sub/direct-messages_" + dmKey;
        messagingTemplate.convertAndSend(destination, saved);
    }
}
