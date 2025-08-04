package com.ayush.ayush.util;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class MessageUtils {

    /**
     * Sends a structured JSON message to a WebSocket client.
     *
     * @param session The client's session.
     * @param type    The type of the message (e.g., "sfu_offer", "existing_producers").
     * @param payload The JSON payload of the message.
     * @throws IOException if the message cannot be sent.
     */
    public static void send(WebSocketSession session, String type, JSONObject payload) throws IOException {
        if (session == null || !session.isOpen()) {
            return;
        }
        JSONObject message = new JSONObject()
                .put("type", type)
                .put("payload", payload);
        session.sendMessage(new TextMessage(message.toString()));
    }
}