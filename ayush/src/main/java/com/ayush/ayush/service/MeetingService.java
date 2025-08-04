package com.ayush.ayush.service;

import com.ayush.ayush.entity.MeetingRoom;
import com.ayush.ayush.repository.MeetingRoomRepository;
import org.springframework.beans.factory.annotation.Autowired; // Ensure this is imported
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class MeetingService {

    private final MeetingRoomRepository meetingRoomRepository;
    private final JanusService janusService;

    // ✅ Using @Autowired on the constructor for robust dependency injection
    @Autowired
    public MeetingService(MeetingRoomRepository meetingRoomRepository, JanusService janusService) {
        this.meetingRoomRepository = meetingRoomRepository;
        this.janusService = janusService;
    }

    public MeetingRoom createMeetingRoom(String description) {
        Long janusRoomId = janusService.createRoom();
        String friendlyId = generateUniqueFriendlyId();
        MeetingRoom newRoom = new MeetingRoom(friendlyId, janusRoomId, description);
        return meetingRoomRepository.save(newRoom);
    }

    public Optional<MeetingRoom> findRoomByFriendlyId(String friendlyId) {
        return meetingRoomRepository.findById(friendlyId);
    }

    private String generateUniqueFriendlyId() {
        String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        SecureRandom random = new SecureRandom();
        return IntStream.range(0, 6)
                .map(i -> random.nextInt(CHARS.length()))
                .mapToObj(randomIndex -> String.valueOf(CHARS.charAt(randomIndex)))
                .collect(Collectors.joining());
    }
}