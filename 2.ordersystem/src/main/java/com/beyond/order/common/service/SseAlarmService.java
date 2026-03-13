package com.beyond.order.common.service;

import com.beyond.order.common.dtos.SseMessageDto;
import com.beyond.order.common.repository.SseEmitterRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.item.mail.MailErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component
public class SseAlarmService implements MessageListener {
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    @Autowired
    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, ObjectMapper objectMapper, @Qualifier("ssePubSub")RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public void sendMessage(String receiver, String sender, String message) {
        SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiver);
        //없을때 아래 dto를 redis로 전송해야함
        SseMessageDto dto = SseMessageDto.builder()
                .receiver(receiver).sender(sender).message(message).build();
        try {
            String data = objectMapper.writeValueAsString(dto);
                                        //메시지의 타이틀     본문
            //사용자정보가 없을때만 pubsub을 이용하고 아니라면 바로 알림구리
//            만약에 emitter객체가 현재서버에 있으면, 바로 알림발송, 그렇지 않으면, redis pub/sub활용.
            if(sseEmitter !=null){
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
//                사용자가 새로고침후에 알림메시지를 조회하려면 DB에 추가적으로 저장 필요.
            }else {
                redisTemplate.convertAndSend("order-channel",data);
            }
////            redis pubsub기능을 활용하여 메시지 publish
//            redisTemplate.convertAndSend("order-channel", data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
//        message : 실질적으로 메시지가 담겨있는 객체
//        pattern : 채널명
//        추후 여러개의 채널에 각기 메시지를 publish하고 subscribe할 경우, 채널명으로 분기처리 가능
        String channelName = new String(pattern);
        System.out.println("channelName : " + channelName);
        try {
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);
            String data = objectMapper.writeValueAsString(dto);
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(dto.getReceiver());
//            해당서버에 receiver의 emitter객체가 있으면 send
            if(sseEmitter!=null){
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
