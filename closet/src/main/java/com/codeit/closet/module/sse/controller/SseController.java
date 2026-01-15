package com.codeit.closet.module.sse.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codeit.closet.module.sse.util.SseEmitterManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

	private final SseEmitterManager emitterManager;

	@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter connect(
		@RequestParam(name = "lastEventId", required = false) String lastEventId
	) {
		UUID receiverId = tryResolveReceiverId();

		SseEmitter emitter;

		if (receiverId == null) {
			// 연결은 열되 emitter 등록 X
			log.warn("[SSE] 인증 정보 없음 -> anonymous 연결");
			emitter = emitterManager.createAnonymousEmitter();
		} else {
			emitter = emitterManager.add(receiverId);
			log.info("[SSE] 인증 사용자 연결, receiverId={}, 현재 연결수={}",
				receiverId, emitterManager.count(receiverId));
		}

		try {
			emitter.send(SseEmitter.event()
				.id(Instant.now().toString())
				.name("connect")
				.data("SSE 연결 성공"));
		} catch (IOException e) {
			log.info("[SSE] 초기 이벤트 전송 실패, receiverId={}, reason={}",
				receiverId, e.getMessage());
			if (receiverId != null) {
				emitterManager.remove(receiverId, emitter);
			}

			emitter.completeWithError(e);
		}

		if (lastEventId != null && !lastEventId.isBlank()) {
			log.info("[SSE] 재연결 감지, receiverId={}, lastEventId={}"
				, receiverId, lastEventId);
		}
		return emitter;
	}

	private UUID tryResolveReceiverId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null
			|| !authentication.isAuthenticated()
			|| authentication instanceof AnonymousAuthenticationToken) {
			return null;
		}

		Object principal = authentication.getPrincipal();

		// 1) principal이 UUID
		if (principal instanceof UUID uuid) {
			return uuid;
		}

		// 2) principal이 String(UUID)
		if (principal instanceof String s) {
			return parseUuidSafely(s);
		}

		// 3) principal 객체에서 getId()/getUserId()
		UUID byReflection = tryExtractUuidByReflection(principal);
		if (byReflection != null) {
			return byReflection;
		}

		// 4) authentication.getName()이 UUID
		String name = authentication.getName();
		if (name != null && !name.isBlank()) {
			return parseUuidSafely(name);
		}

		return null;
	}

	private UUID tryExtractUuidByReflection(Object principal) {
		try {
			Method method = principal.getClass().getMethod("getId");
			Object value = method.invoke(principal);
			return parseUuidObject(value);
		} catch (Exception ignored) {}

		try {
			Method method = principal.getClass().getMethod("getUserId");
			Object value = method.invoke(principal);
			return parseUuidObject(value);
		} catch (Exception ignored) {}

		return null;
	}

	private UUID parseUuidObject(Object value) {
		if (value instanceof UUID uuid) return uuid;
		if (value instanceof String s) return parseUuidSafely(s);
		return null;
	}

	private UUID parseUuidSafely(String value) {
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException e) {
			log.warn("[SSE] UUID 파싱 실패: {}", value);
			return null;
		}
	}
}
