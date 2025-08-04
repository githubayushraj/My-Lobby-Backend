package com.ayush.ayush.service;

import com.ayush.ayush.participant.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SfuService {

    private static final Logger logger = LoggerFactory.getLogger(SfuService.class);

    // SfuService depends on RoomManager to keep track of users
    private final RoomManager roomManager;
    private final ObjectMapper objectMapper;

    public SfuService(RoomManager roomManager, ObjectMapper objectMapper) {
        this.roomManager = roomManager;
        this.objectMapper = objectMapper;
    }

    /**
     * Handles a new user joining a room. This is the most critical method for solving the "late joiner" issue.
     */
    public void handleJoin(UserSession userSession) {
        // Step 1: Get a list of all participants who are ALREADY in the room.
        // This is done before the new user is officially added.
        var existingParticipants = roomManager.getParticipantsInRoom(userSession.getRoomId())
                .map(participants -> participants.keySet().stream()
                        .filter(id -> !id.equals(userSession.getUserId())) // Exclude the user themselves
                        .collect(Collectors.toSet()))
                .orElse(java.util.Collections.emptySet()); // If room is new, this will be empty

        // Step 2: Add the new user to the room's central directory.
        roomManager.addUserToRoom(userSession);

        // Step 3: Send the list of existing users ONLY to the new user.
        // This tells their client: "Welcome! You need to establish a connection with these people."
        logger.info("Sending existing_participants list ({}) to new user {}", existingParticipants, userSession.getUserId());
        sendMessage(userSession, "existing_participants", Map.of("userIds", existingParticipants));

        // Step 4: Announce the new user's arrival to everyone else in the room.
        // This tells existing clients: "A new person has joined. Wait for their connection offer."
        logger.info("Broadcasting new_participant ({}) to the room {}", userSession.getUserId(), userSession.getRoomId());
        broadcast(userSession, "new_participant", Map.of("userId", userSession.getUserId()));
    }

    /**
     * Forwards a WebRTC SDP offer from a sender to a specific target user.
     */
    public void handleOffer(UserSession sender, Map<String, Object> payload) {
        String remoteUserId = (String) payload.get("remoteUserId");
        Object sdp = payload.get("sdp");
        logger.info("Forwarding offer from {} to {}", sender.getUserId(), remoteUserId);
        forwardToParticipant(sender, remoteUserId, "offer", Map.of("userId", sender.getUserId(), "sdp", sdp));
    }

    /**
     * Forwards a WebRTC SDP answer from a sender to a specific target user.
     */
    public void handleAnswer(UserSession sender, Map<String, Object> payload) {
        String remoteUserId = (String) payload.get("remoteUserId");
        Object sdp = payload.get("sdp");
        logger.info("Forwarding answer from {} to {}", sender.getUserId(), remoteUserId);
        forwardToParticipant(sender, remoteUserId, "answer", Map.of("userId", sender.getUserId(), "sdp", sdp));
    }

    /**
     * Forwards a WebRTC ICE candidate from a sender to a specific target user.
     */
    public void handleIceCandidate(UserSession sender, Map<String, Object> payload) {
        String remoteUserId = (String) payload.get("remoteUserId");
        Object candidate = payload.get("candidate");
        logger.info("Forwarding ICE candidate from {} to {}", sender.getUserId(), remoteUserId);
        forwardToParticipant(sender, remoteUserId, "ice_candidate", Map.of("userId", sender.getUserId(), "candidate", candidate));
    }

    /**
     * Handles a user disconnecting. It finds the user by their WebSocket session ID,
     * removes them from the room, and notifies others.
     */
    public void handleLeave(String sessionId) {
        roomManager.removeUserFromRoom(sessionId).ifPresent(removedUser -> {
            logger.info("User {} left room {}. Notifying others.", removedUser.getUserId(), removedUser.getRoomId());
            broadcast(removedUser, "participant_left", Map.of("userId", removedUser.getUserId()));
        });
    }

    // --- Private Helper Methods ---

    /**
     * Sends a message to a single, specific user session.
     */
    private void sendMessage(UserSession session, String type, Object payload) {
        try {
            // The message structure { "type": "...", "payload": { ... } } matches the frontend's expectation.
            Map<String, Object> message = Map.of("type", type, "payload", payload);
            session.getWebSocketSession().sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            logger.error("Failed to send message to user {}: {}", session.getUserId(), e.getMessage());
        }
    }

    /**
     * Finds a specific user in a room and forwards a message to them.
     */
    private void forwardToParticipant(UserSession sender, String targetUserId, String type, Object payload) {
        roomManager.getParticipant(sender.getRoomId(), targetUserId)
                .ifPresentOrElse(
                        targetUser -> sendMessage(targetUser, type, payload),
                        () -> logger.warn("Could not find participant {} in room {} to forward message", targetUserId, sender.getRoomId())
                );
    }

    /**
     * Sends a message to every participant in a room, except for the original sender.
     */
    private void broadcast(UserSession sender, String type, Object payload) {
        roomManager.getParticipantsInRoom(sender.getRoomId()).ifPresent(participants -> {
            participants.values().stream()
                    .filter(session -> !session.getUserId().equals(sender.getUserId()))
                    .forEach(recipient -> sendMessage(recipient, type, payload));
        });
    }
}