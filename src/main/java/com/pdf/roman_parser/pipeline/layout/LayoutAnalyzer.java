package com.pdf.roman_parser.pipeline.layout;

import com.pdf.roman_parser.pipeline.model.LayoutBlock;
import com.pdf.roman_parser.pipeline.model.PageRaster;

import java.io.IOException;
import java.util.List;

public interface LayoutAnalyzer {
    List<LayoutBlock> analyze(PageRaster pageRaster) throws IOException;
}
