package com.pdf.roman_parser.pipeline.pdf;

import com.pdf.roman_parser.pipeline.model.PageRaster;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfBoxPageRenderer implements PdfPageRenderer {

    private static final float DPI = 200f;

    @Override
    public List<PageRaster> render(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            List<PageRaster> pages = new ArrayList<>(document.getNumberOfPages());
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, DPI, ImageType.RGB);
                var mediaBox = document.getPage(i).getMediaBox();
                pages.add(new PageRaster(i, image, mediaBox.getWidth(), mediaBox.getHeight()));
            }
            return pages;
        }
    }
}
