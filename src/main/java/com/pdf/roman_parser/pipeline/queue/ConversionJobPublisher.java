package com.pdf.roman_parser.pipeline.queue;

import java.util.UUID;

public interface ConversionJobPublisher {
    UUID publish(byte[] pdfBytes);
}
