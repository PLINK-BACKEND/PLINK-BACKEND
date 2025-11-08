package com.plink.backend.config;

import com.plink.backend.game.websocket.GameWebSocketHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@Order(1)
@RequiredArgsConstructor
public class GameWebSocketConfig implements WebSocketConfigurer {

    private final GameWebSocketHandler gameWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("✅ registerWebSocketHandlers() called");
        registry.addHandler(gameWebSocketHandler, "/ws/game")
                .setAllowedOriginPatterns("*");
                //.withSockJS(); // 포스트맨 테스트할 때는 필요했음!
    }

    // 콘솔 테스트용
    @PostConstruct
    public void init() {
        System.out.println("✅ GameWebSocketConfig loaded successfully!");
    }
}
