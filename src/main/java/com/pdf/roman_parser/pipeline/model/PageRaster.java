package com.pdf.roman_parser.pipeline.model;

import java.awt.image.BufferedImage;

public record PageRaster(int pageIndex, BufferedImage image, double widthPts, double heightPts) {
}
