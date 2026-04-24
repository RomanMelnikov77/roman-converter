package com.pdf.roman_parser.service;

import com.pdf.roman_parser.docx.OpenXmlDocxGenerator;
import com.pdf.roman_parser.pipeline.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenXmlDocxGeneratorTest {

    @Test
    void shouldGenerateNonEmptyArchive() throws Exception {
        OpenXmlDocxGenerator generator = new OpenXmlDocxGenerator();
        DocumentModel model = new DocumentModel(List.of(
                new PageModel(0, 500, 700, List.of(
                        new OcrBlockResult(0, new BoundingBox(10, 20, 100, 30), "مرحبا", true),
                        new OcrBlockResult(0, new BoundingBox(10, 60, 100, 30), "hello", false)
                ))
        ));

        byte[] data = generator.generate(model);

        assertTrue(data.length > 200);
    }
}
