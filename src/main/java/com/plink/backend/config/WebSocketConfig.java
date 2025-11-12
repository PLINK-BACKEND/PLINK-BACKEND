package com.plink.backend.config;


import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/feed")
                .setAllowedOriginPatterns("*")// 추후 보안면에서 수정 (프론트 도메인만 허용하도록)
        .withSockJS();

    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 서버 -> 클라이언트
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트 -> 서버
        registry.setApplicationDestinationPrefixes("/app");


    }

    @PostConstruct
    public void init() {
        System.out.println("✅ WebSocketConfig (STOMP broker) loaded successfully!");
    }
}
