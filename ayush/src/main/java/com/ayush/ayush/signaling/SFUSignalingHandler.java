package com.ayush.ayush.signaling;

import com.ayush.ayush.participant.UserSession;
import com.ayush.ayush.service.SfuService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SFUSignalingHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(SFUSignalingHandler.class);

    private final SfuService sfuService;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, UserSession> sessions = new ConcurrentHashMap<>();

    public SFUSignalingHandler(SfuService sfuService, ObjectMapper objectMapper) {
        this.sfuService = sfuService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // ✅ ADDED LOG: More explicit confirmation of connection.
        logger.info("✅ Connection established: {}. Waiting for 'join' message...", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // ✅ ADDED LOG: This is the most important new log. It shows you exactly what the server received.
        logger.info("⬇️ Message received from [{}]: {}", session.getId(), message.getPayload());

        try {
            Map<String, Object> msg = objectMapper.readValue(message.getPayload(), new TypeReference<>() {});
            String type = (String) msg.get("type");
            Map<String, Object> payload = (Map<String, Object>) msg.get("payload");

            if (type == null || payload == null) {
                logger.warn("⚠️ Malformed message from session {}: Missing 'type' or 'payload'", session.getId());
                return;
            }

            // ✅ ADDED LOG: Shows which message type is being processed.
            logger.info("➡️ Processing message type '{}' for session {}", type, session.getId());

            switch (type) {
                case "join":
                    String roomId = (String) payload.get("roomId");
                    String userId = (String) payload.get("userId");
                    if (roomId == null || userId == null) {
                        logger.warn("⚠️ 'join' message is missing 'roomId' or 'userId'");
                        return;
                    }
                    UserSession newUserSession = new UserSession(userId, roomId, session);
                    sessions.put(session.getId(), newUserSession);
                    sfuService.handleJoin(newUserSession);
                    break;

                default:
                    UserSession senderSession = sessions.get(session.getId());
                    if (senderSession == null) {
                        logger.warn("⚠️ Message received from a session that has not joined a room: {}", session.getId());
                        return;
                    }

                    switch (type) {
                        case "offer":
                            sfuService.handleOffer(senderSession, payload);
                            break;
                        case "answer":
                            sfuService.handleAnswer(senderSession, payload);
                            break;
                        case "ice_candidate":
                            sfuService.handleIceCandidate(senderSession, payload);
                            break;
                        default:
                            logger.warn("⚠️ Unknown message type '{}' from session {}", type, session.getId());
                    }
            }
        } catch (IOException e) {
            logger.error("❌ Error parsing message from session {}: {}", session.getId(), message.getPayload(), e);
        } catch (Exception e) {
            logger.error("❌ An unexpected error occurred while handling a message from session {}:", session.getId(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("❌ Connection closed: {} with status: {}", session.getId(), status);
        UserSession removedSession = sessions.remove(session.getId());
        if (removedSession != null) {
            sfuService.handleLeave(removedSession.getWebSocketSession().getId());
        } else {
            // ✅ ADDED LOG: For cases where a connection closes before joining.
            logger.info("A session that never joined a room has disconnected.");
        }
    }
}