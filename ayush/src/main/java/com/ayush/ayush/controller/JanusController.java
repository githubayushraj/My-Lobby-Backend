package com.ayush.ayush.controller;

import com.ayush.ayush.service.JanusService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;


@RestController
@RequestMapping("/api/janus")
public class JanusController {

    private final JanusService janusService;

    public JanusController(JanusService janusService) {
        this.janusService = janusService;
    }

    @PostMapping("/create-room")
    public Long createRoom() {
        return janusService.createRoom();
    }
}
