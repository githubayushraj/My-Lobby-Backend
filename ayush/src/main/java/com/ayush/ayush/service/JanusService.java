package com.ayush.ayush.service;

import com.ayush.ayush.util.RoomUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service to interact with the Janus WebRTC Server.
 * This version uses robust logging and error handling but avoids @Value for stability.
 */
@Service
public class JanusService {

    private static final Logger logger = LoggerFactory.getLogger(JanusService.class);

    // Dependencies injected by Spring.
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ✅ REVERTED: The URL is now hardcoded again to avoid @Value startup errors.
    // This is the most stable approach for your current setup.
    private static final String JANUS_URL = "http://localhost:8088/janus";

    /**
     * Constructor for dependency injection.
     * ✅ REMOVED: The @Value annotation is gone to ensure the application starts reliably.
     */
    @Autowired
    public JanusService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new video room on the Janus server.
     */
    public Long createRoom() {
        long sessionId = -1;
        try {
            // ✅ KEPT: The improved logging.
            logger.info("Step 1: Creating Janus session at URL: {}", JANUS_URL);
            String sessionResponse = restTemplate.postForObject(JANUS_URL, Map.of("janus", "create", "transaction", "txn1"), String.class);
            sessionId = extractSessionId(sessionResponse);
            logger.info("Successfully created Janus session with ID: {}", sessionId);

            String attachUrl = JANUS_URL + "/" + sessionId;
            String attachResponse = restTemplate.postForObject(attachUrl, Map.of("janus", "attach", "plugin", "janus.plugin.videoroom", "transaction", "txn2"), String.class);
            long handleId = extractHandleId(attachResponse);
            logger.info("Successfully attached plugin handle with ID: {}", handleId);

            String pluginUrl = JANUS_URL + "/" + sessionId + "/" + handleId;
            long roomId = RoomUtil.generateRoomId();
            Map<String, Object> body = Map.of("request", "create", "room", roomId, "description", "Video Meeting Room", "publishers", 10);
            Map<String, Object> message = Map.of("janus", "message", "body", body, "transaction", "txn3");
            String createRoomResponse = restTemplate.postForObject(pluginUrl, message, String.class);
            logger.info("Received 'create room' response: {}", createRoomResponse);

            return roomId;

        } catch (RestClientException e) {
            // ✅ KEPT: The improved error handling.
            logger.error("Network error while communicating with Janus server at {}", JANUS_URL, e);
            throw new RuntimeException("Could not connect to Janus server. Please ensure it is running and accessible.", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during Janus room creation for session ID: {}", sessionId, e);
            throw new RuntimeException("Failed to create Janus room due to an unexpected error.", e);
        }
    }

    private long extractSessionId(String response) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(response);
        if (root.path("janus").asText().equals("error")) {
            throw new RuntimeException("Janus returned an error while creating session: " + response);
        }
        return root.path("data").path("id").asLong();
    }

    private long extractHandleId(String response) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(response);
        if (root.path("janus").asText().equals("error")) {
            throw new RuntimeException("Janus returned an error while attaching plugin: " + response);
        }
        return root.path("data").path("id").asLong();
    }
}