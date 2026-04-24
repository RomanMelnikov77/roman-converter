package com.pdf.roman_parser.pipeline.queue;

import com.pdf.roman_parser.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConversionJobListener {

    private static final Logger log = LoggerFactory.getLogger(ConversionJobListener.class);

    @RabbitListener(queues = RabbitConfig.CONVERT_QUEUE)
    public void receive(byte[] pdfBytes) {
        log.info("Queued conversion task accepted, payload size={} bytes", pdfBytes.length);
    }
}
