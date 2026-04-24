package com.pdf.roman_parser.docx;

import com.pdf.roman_parser.pipeline.model.DocumentModel;

import java.io.IOException;

public interface DocxGenerator {
    byte[] generate(DocumentModel model) throws IOException;
}
