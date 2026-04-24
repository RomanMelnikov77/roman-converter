package com.pdf.roman_parser.pipeline.model;

public record OcrBlockResult(int pageIndex, BoundingBox boundingBox, String text, boolean rtl) {
}
