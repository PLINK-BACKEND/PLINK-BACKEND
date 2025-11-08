package com.plink.backend.game.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    // slug별로 세션을 따로 저장
    private final Map<String, Set<WebSocketSession>> slugSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("게임 웹소켓 연결됨: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSessionFromSlug(session);
        log.info("게임 웹소켓 종료됨: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 클라이언트가 slug를 첫 메시지로 보내면 방에 등록
        String slug = message.getPayload();
        addSessionToSlug(slug, session);
        log.info("[{}] 게임 slug 구독 등록됨: {}", slug, session.getId());
    }

    // 특정 slug에 해당하는 사람들에게만 broadcast
    public void broadcastToSlug(String slug, String message) {
        Set<WebSocketSession> sessions = slugSessionMap.get(slug);
        if (sessions == null) {
            log.warn("게임 slug [{}]에 연결된 세션이 없습니다.", slug);
            return;
        }

        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error("게임 slug [{}] 메시지 전송 실패: {}", slug, session.getId(), e);
            }
        }
    }

    // slug → session 등록
    private void addSessionToSlug(String slug, WebSocketSession session) {
        slugSessionMap.computeIfAbsent(slug, k -> ConcurrentHashMap.newKeySet()).add(session);
        session.getAttributes().put("slug", slug);
    }

    // 세션이 종료될 때 slug에서 제거
    private void removeSessionFromSlug(WebSocketSession session) {
        String slug = (String) session.getAttributes().get("slug");
        if (slug != null) {
            Set<WebSocketSession> sessions = slugSessionMap.get(slug);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) slugSessionMap.remove(slug);
            }
        }
    }

    public GameWebSocketHandler() {
        System.out.println("✅ GameWebSocketHandler bean created");
    }
}
