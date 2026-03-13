package com.beyond.order.common.controller;

import com.beyond.order.common.repository.SseEmitterRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/sse")
public class SseController {
    private final SseEmitterRegistry sseEmitterRegistry;

    public SseController(SseEmitterRegistry sseEmitterRegistry) {
        this.sseEmitterRegistry = sseEmitterRegistry;
    }

    @GetMapping("/connect")
    public SseEmitter connect(@RequestHeader("X-User-Email")String email) throws IOException {
        SseEmitter sseEmitter = new SseEmitter(60*60*1000L);//1시간 유효시간
        sseEmitterRegistry.addSseEmitter(email, sseEmitter);
        sseEmitter.send(SseEmitter.event().name("connect").data("연결완료"));
        return sseEmitter;
    }

    @GetMapping("/disconnect")
    public void disconnect(@RequestHeader("X-User-Email")String email) throws IOException {
        sseEmitterRegistry.removeEmitter(email);
    }
}
