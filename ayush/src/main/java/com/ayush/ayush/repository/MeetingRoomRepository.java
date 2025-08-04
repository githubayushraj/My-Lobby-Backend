package com.ayush.ayush.repository;

import com.ayush.ayush.entity.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, String> {
}
