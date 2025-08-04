package com.ayush.ayush.config;

import com.ayush.ayush.signaling.SFUSignalingHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket // This annotation is essential. It enables the WebSocket server functionality.
public class WebSocketConfig implements WebSocketConfigurer {

    private final SFUSignalingHandler sfuSignalingHandler;

    // Spring will automatically inject your SFUSignalingHandler bean here.
    public WebSocketConfig(SFUSignalingHandler sfuSignalingHandler) {
        this.sfuSignalingHandler = sfuSignalingHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // This line tells Spring:
        // 1. Use our SFUSignalingHandler to handle WebSocket messages.
        // 2. Listen for incoming connections on the "/signaling" path.
        // 3. Allow connections from any origin ("*"). This is crucial for development.
        registry.addHandler(sfuSignalingHandler, "/signaling").setAllowedOrigins("*");
    }
}