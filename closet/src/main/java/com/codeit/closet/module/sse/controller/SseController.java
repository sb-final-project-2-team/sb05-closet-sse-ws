package com.codeit.closet.module.sse.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codeit.closet.module.sse.util.SseEmitterManager;
import com.codeit.closet.module.sse.util.SseJwtResolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

	private final SseEmitterManager emitterManager;
	private final SseJwtResolver jwtResolver;

	@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter connect(HttpServletRequest request) {
		UUID receiverId = jwtResolver.resolveReceiverId(request);

		if (receiverId == null) {
			log.warn("[SSE] 사용자 식별 실패 (쿠키 없음");
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		SseEmitter emitter = emitterManager.add(receiverId);

		try {
			emitter.send(SseEmitter.event()
				.name("ping"));

			log.info("[SSE] 인증 연결 성공, receiverId={}, 현재 연결 수 ={}",
				receiverId, emitterManager.count(receiverId));


		} catch (IOException e) {
		    log.error("[SSE] 초기 메시지 전송 실패, receiverId={}, reason={}",
				receiverId, e.getMessage());
			emitterManager.remove(receiverId,emitter);
			emitter.completeWithError(e);
		}
		return emitter;
	}
}
