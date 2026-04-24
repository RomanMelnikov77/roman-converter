package com.pdf.roman_parser.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String CONVERT_QUEUE = "pdf.convert.queue";

    @Bean
    public Queue convertQueue() {
        return new Queue(CONVERT_QUEUE, true);
    }
}
