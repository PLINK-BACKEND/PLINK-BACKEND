package com.plink.backend.tracking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TrackingService {

    // slug별 접속 세션 ID 관리
    private final Map<String, Set<String>> sessionsBySlug = new ConcurrentHashMap<>();

    //사용자가 특정 축제 slug로 연결 시 호출
    public void addConnection(String slug, String sessionId) {
        sessionsBySlug
                .computeIfAbsent(slug, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
        log.info("[{}] 접속자 +1 → 현재 {}명", slug, sessionsBySlug.get(slug).size());
    }

    //연결 해제 시 호출
    public void removeConnection(String slug, String sessionId) {
        Set<String> sessions = sessionsBySlug.get(slug);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) sessionsBySlug.remove(slug);
            log.info("[{}] 접속자 -1 → 현재 {}명", slug, sessions.size());
        }
    }

    //특정 축제의 접속자 수
    public int getActiveCount(String slug) {
        return sessionsBySlug.getOrDefault(slug, Set.of()).size();
    }

    //전체 축제별 접속자 수
    public Map<String, Integer> getAllCounts() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        sessionsBySlug.forEach((slug, sessions) -> result.put(slug, sessions.size()));
        return result;
    }
}