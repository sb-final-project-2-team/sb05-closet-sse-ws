package com.codeit.closet.module.ws.config;

import com.codeit.closet.module.ws.dto.DirectMessageDTO;
import com.codeit.closet.module.ws.dto.DirectMessageSaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class DirectMessageApiClient {

    private final RestClient dmApiRestClient;

    public DirectMessageDTO save(String accessToken, DirectMessageSaveRequest request) {
        return dmApiRestClient.post()
                .uri("/api/direct-messages")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // 토큰 전송
                .body(request)
                .retrieve()
                .body(DirectMessageDTO.class);
    }
}

