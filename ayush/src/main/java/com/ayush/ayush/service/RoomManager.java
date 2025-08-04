package com.ayush.ayush.service;

import com.ayush.ayush.participant.UserSession;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RoomManager {

    // A map of [roomId] to another map of [userId, UserSession]
    private final ConcurrentMap<String, ConcurrentMap<String, UserSession>> rooms = new ConcurrentHashMap<>();

    /**
     * Adds a user's session to the specified room.
     */
    public void addUserToRoom(UserSession userSession) {
        rooms.computeIfAbsent(userSession.getRoomId(), k -> new ConcurrentHashMap<>())
                .put(userSession.getUserId(), userSession);
    }

    /**
     * Removes a user from whatever room they are in, using their WebSocket session ID.
     * This is critical for cleanup when a user disconnects.
     * @param webSocketId The ID from the WebSocketSession that has been closed.
     * @return The UserSession of the user who was removed, if they were found.
     */
    public Optional<UserSession> removeUserFromRoom(String webSocketId) {
        for (ConcurrentMap<String, UserSession> room : rooms.values()) {
            Optional<String> userIdToRemove = room.values().stream()
                    .filter(session -> session.getWebSocketSession().getId().equals(webSocketId))
                    .map(UserSession::getUserId)
                    .findFirst();

            if (userIdToRemove.isPresent()) {
                UserSession removedUser = room.remove(userIdToRemove.get());
                // If the room becomes empty after the user leaves, remove the room itself.
                if (room.isEmpty()) {
                    rooms.remove(removedUser.getRoomId());
                }
                return Optional.ofNullable(removedUser);
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all participants currently in a given room.
     */
    public Optional<ConcurrentMap<String, UserSession>> getParticipantsInRoom(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    /**
     * Retrieves a specific participant from a specific room.
     */
    public Optional<UserSession> getParticipant(String roomId, String userId) {
        return Optional.ofNullable(rooms.get(roomId)).map(room -> room.get(userId));
    }
}