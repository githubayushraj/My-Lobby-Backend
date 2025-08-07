package com.ayush.ayush.controller;

import com.ayush.ayush.entity.MeetingRoom;
import com.ayush.ayush.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired; // Ensure this is imported
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/meetings")
@CrossOrigin(origins = {
    "https://my-lobby-frontend.vercel.app", 
    "http://localhost:5173"
})
    public class MeetingController {

    private final MeetingService meetingService;

    // ✅ Using @Autowired on the constructor is the best practice for injection.
    // This guarantees meetingService will never be null.
    @Autowired
    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping("/create")
    public MeetingRoom createMeeting(@RequestBody Map<String, String> payload) {
        String description = payload.getOrDefault("description", "New Meeting");
        return meetingService.createMeetingRoom(description);
    }

    @GetMapping("/join/{friendlyId}")
    public ResponseEntity<?> joinMeeting(@PathVariable String friendlyId) {
        try {
            Optional<MeetingRoom> roomOptional = meetingService.findRoomByFriendlyId(friendlyId.toUpperCase());

            if (roomOptional.isPresent()) {
                MeetingRoom room = roomOptional.get();
                return ResponseEntity.ok(Map.of(
                        "isValid", true,
                        "janusRoomId", room.getJanusRoomId()
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of("isValid", false, "error", "Room not found"));
            }
        } catch (Exception e) {
            // Log the exception on the server side for debugging
            System.err.println("Error in joinMeeting: " + e.getMessage());
            e.printStackTrace();
            // Return a 500 error with a clear message
            return ResponseEntity.status(500).body(Map.of("isValid", false, "error", "An unexpected server error occurred."));
        }
    }
}
