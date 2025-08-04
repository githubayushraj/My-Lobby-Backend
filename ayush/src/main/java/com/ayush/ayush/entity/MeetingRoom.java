    package com.ayush.ayush.entity;

    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import java.time.LocalDateTime;

    @Entity
    @Table(name = "meeting_rooms")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class MeetingRoom {

        @Id
        private String friendlyRoomId; // This becomes the primary key column

        @Column(unique = true, nullable = false)
        private Long janusRoomId; // This is a required, unique column

        private String description;
        private LocalDateTime createdAt;

        public MeetingRoom(String friendlyRoomId, Long janusRoomId, String description) {
            this.friendlyRoomId = friendlyRoomId;
            this.janusRoomId = janusRoomId;
            this.description = description;
            this.createdAt = LocalDateTime.now();
        }

        public void setRoomId(String roomId) {
        }
    }