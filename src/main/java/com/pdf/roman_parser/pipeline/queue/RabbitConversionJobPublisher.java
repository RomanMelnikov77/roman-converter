package com.pdf.roman_parser.pipeline.queue;

import com.pdf.roman_parser.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Primary
public class RabbitConversionJobPublisher implements ConversionJobPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitConversionJobPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitConversionJobPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public UUID publish(byte[] pdfBytes) {
        UUID jobId = UUID.randomUUID();
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.CONVERT_QUEUE, pdfBytes);
        } catch (AmqpException ex) {
            log.warn("RabbitMQ unavailable, conversion task {} is processed inline only", jobId, ex);
        }
        return jobId;
    }
}
