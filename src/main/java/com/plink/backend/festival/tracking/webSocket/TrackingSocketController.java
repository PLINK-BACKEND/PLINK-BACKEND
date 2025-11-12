package com.plink.backend.festival.tracking.webSocket;

import com.plink.backend.festival.tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TrackingSocketController {

    private final TrackingService trackingService;
    private final SimpMessagingTemplate messagingTemplate;

    // STOMP 구독 시 slug 전달 → 예: /app/festival/join/{slug}
    @MessageMapping("/festival/join/{slug}")
    public void handleJoin(@DestinationVariable String slug, org.springframework.messaging.Message<?> message) {
        String sessionId = (String) message.getHeaders().get("simpSessionId");
        trackingService.addConnection(slug, sessionId);
        broadcastCount(slug);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        // slug 찾기 (간단히 모든 slug 조회)
        trackingService.getAllCounts().keySet().forEach(slug -> {
            trackingService.removeConnection(slug, sessionId);
            broadcastCount(slug);
        });
    }

    private void broadcastCount(String slug) {
        int count = trackingService.getActiveCount(slug);
        messagingTemplate.convertAndSend("/topic/festival/" + slug + "/users",
                Map.of("slug", slug, "activeUsers", count));
    }
}
