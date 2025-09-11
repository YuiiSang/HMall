package com.hmall.common.config;

import org.checkerframework.checker.units.qual.C;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Title: MQConfig
 * @Author Tanght 363993584@qq.com
 * @Date 2025/8/6 17:25
 * @description:
 */
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
public class MQConfig {
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
