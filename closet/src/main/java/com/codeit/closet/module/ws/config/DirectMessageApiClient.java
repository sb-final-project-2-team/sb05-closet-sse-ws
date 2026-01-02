package com.codeit.closet.module.ws.config;

import com.codeit.closet.module.ws.dto.DirectMessageDTO;
import com.codeit.closet.module.ws.dto.DirectMessagePersistRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class DirectMessageApiClient {

    private final RestClient dmApiRestClient;

    public DirectMessageDTO persist(DirectMessagePersistRequest request) {
        return dmApiRestClient.post()
                .uri("/api/direct-messages")
                .body(request)
                .retrieve()
                .body(DirectMessageDTO.class);
    }
}

