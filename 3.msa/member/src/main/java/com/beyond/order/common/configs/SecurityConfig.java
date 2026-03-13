package com.beyond.order.common.configs;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder pwEncoder(){
//        들어가서 Component 붙이고싶은데 안되어서 Bean사용 메서드를 통해 싱글톤객체만들구리
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
