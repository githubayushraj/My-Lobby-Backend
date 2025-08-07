package com.ayush.ayush.controller;

import com.ayush.ayush.service.JanusService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;


@RestController
@RequestMapping("/api/janus")
@CrossOrigin(origins = {
    "https://my-lobby-frontend.vercel.app", 
    "http://localhost:5173"
})
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
