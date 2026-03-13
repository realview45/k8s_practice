package com.beyond.order.common.configs;

import com.beyond.order.common.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
//    연결빈객체(0~9번인지)호스트, 포트정보
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory ssePubSubConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        //redis pub/sub기능은 db에 값을 저장하는 기능이 아니므로, 특정db에 의존적이지 않음.
        return new LettuceConnectionFactory();
    }
    @Bean//이 빈을 주입받아 redis에 저장할 예정
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String>ssePubSubRedisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());//다문자열이지만 이건리스트 셋,해시스,지셋이라는 태그를 가진다.
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
//    redis 리스너(subscribe) 객체
//                   listen중
//    호출구조 RedisMessageListenerContainer -> MessageListenerAdapter -> SseAlarmService(MessageListener객체를 구현한)
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory,
                                                                       @Qualifier("ssePubSub") MessageListenerAdapter messageListenerAdapter){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel"));
//        만약에 여러채널을 구독해야 하는 경우, 여러개의 PatternTopic을 add하거나, 별도의 Listener Bean 객체 생성
        return container;
    }
//    redis에서 수신된 메시지를 처리하는 객체
    @Bean
    @Qualifier("ssePubSub")
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService){
//        채널로부터 수신되는 message처리를 SseAlarmService의 onMessage메서드로 위임
        return new MessageListenerAdapter(sseAlarmService, "onMessage");
    }
}
