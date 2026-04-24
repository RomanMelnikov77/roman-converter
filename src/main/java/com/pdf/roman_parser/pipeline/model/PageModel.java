package com.pdf.roman_parser.pipeline.model;

import java.util.List;

public record PageModel(int pageIndex, double width, double height, List<OcrBlockResult> blocks) {
}
