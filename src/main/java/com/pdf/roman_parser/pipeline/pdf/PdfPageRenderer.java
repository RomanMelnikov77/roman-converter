package com.pdf.roman_parser.pipeline.pdf;

import com.pdf.roman_parser.pipeline.model.PageRaster;

import java.io.IOException;
import java.util.List;

public interface PdfPageRenderer {
    List<PageRaster> render(byte[] pdfBytes) throws IOException;
}
