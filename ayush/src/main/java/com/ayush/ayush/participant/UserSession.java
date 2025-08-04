package com.ayush.ayush.participant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

@Getter
@RequiredArgsConstructor // Creates a constructor for final fields
public class UserSession {
    private final String userId;
    private final String roomId;
    private final WebSocketSession webSocketSession;
}