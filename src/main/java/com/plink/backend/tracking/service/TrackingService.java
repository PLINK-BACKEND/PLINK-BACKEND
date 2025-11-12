package com.plink.backend.tracking.service;

import com.plink.backend.main.entity.Festival;
import com.plink.backend.main.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final FestivalRepository festivalRepository;

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

    // 전체 축제별 접속자 수 (없으면 0으로 표시)
    public Map<String, Integer> getAllCounts() {
        Map<String, Integer> result = new ConcurrentHashMap<>();

        // DB에서 모든 축제의 slug 조회
        List<String> festivalSlugs = festivalRepository.findAll()
                .stream()
                .map(Festival::getSlug)
                .toList();

        // 기본값 0으로 초기화
        festivalSlugs.forEach(slug -> result.put(slug, 0));

        // 실제 접속 중인 축제 반영
        sessionsBySlug.forEach((slug, sessions) -> result.put(slug, sessions.size()));

        return result;
    }
}