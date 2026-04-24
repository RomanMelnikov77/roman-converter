package com.pdf.roman_parser.pipeline.model;

public record LayoutBlock(int pageIndex, BoundingBox boundingBox, byte[] imagePng) {
}
