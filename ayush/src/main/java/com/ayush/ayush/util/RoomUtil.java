package com.ayush.ayush.util;

import java.util.Random;

public class RoomUtil {
    public static long generateRoomId() {
        Random random = new Random();
        // Janus room IDs should be > 0
        return 100000 + random.nextInt(900000); // 6-digit room ID
    }
}
