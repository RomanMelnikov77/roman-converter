package com.pdf.roman_parser.service;

import com.pdf.roman_parser.docx.DocxGenerator;
import com.pdf.roman_parser.pipeline.layout.LayoutAnalyzer;
import com.pdf.roman_parser.pipeline.model.DocumentModel;
import com.pdf.roman_parser.pipeline.model.OcrBlockResult;
import com.pdf.roman_parser.pipeline.model.PageModel;
import com.pdf.roman_parser.pipeline.model.PageRaster;
import com.pdf.roman_parser.pipeline.ocr.OcrEngine;
import com.pdf.roman_parser.pipeline.pdf.PdfPageRenderer;
import com.pdf.roman_parser.pipeline.postprocess.TextPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class PdfToDocxService {
    private static final Logger log = LoggerFactory.getLogger(PdfToDocxService.class);

    private final PdfPageRenderer pageRenderer;
    private final LayoutAnalyzer layoutAnalyzer;
    private final OcrEngine ocrEngine;
    private final TextPostProcessor textPostProcessor;
    private final DocxGenerator docxGenerator;
    private final Executor executor;

    public PdfToDocxService(PdfPageRenderer pageRenderer,
                            LayoutAnalyzer layoutAnalyzer,
                            OcrEngine ocrEngine,
                            TextPostProcessor textPostProcessor,
                            DocxGenerator docxGenerator,
                            @Qualifier("pipelineExecutor") Executor executor) {
        this.pageRenderer = pageRenderer;
        this.layoutAnalyzer = layoutAnalyzer;
        this.ocrEngine = ocrEngine;
        this.textPostProcessor = textPostProcessor;
        this.docxGenerator = docxGenerator;
        this.executor = executor;
    }

    public CompletableFuture<byte[]> convert(byte[] pdfBytes) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Starting PDF to DOCX conversion");
                log.info("Stage 1/4: Rendering PDF pages");
                List<PageRaster> pages = pageRenderer.render(pdfBytes);
                log.info("Stage 1/4 completed: Rendered {} page(s)", pages.size());

                log.info("Stage 2/4: Processing rendered pages");
                List<CompletableFuture<PageModel>> pageTasks = pages.stream()
                        .map(this::processPage)
                        .toList();

                List<PageModel> pageModels = pageTasks.stream()
                        .map(CompletableFuture::join)
                        .sorted(Comparator.comparing(PageModel::pageIndex))
                        .toList();
                log.info("Stage 2/4 completed: Processed {} page model(s)", pageModels.size());

                log.info("Stage 3/4: Generating DOCX");
                return docxGenerator.generate(new DocumentModel(pageModels));
            } catch (IOException e) {
                log.error("PDF conversion failed", e);
                throw new IllegalStateException("PDF conversion failed", e);
            } finally {
                log.info("Stage 4/4: Conversion pipeline finished");
            }
        }, executor);
    }

    private CompletableFuture<PageModel> processPage(PageRaster page) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Page {}: Starting layout analysis", page.pageIndex());
                var blocks = layoutAnalyzer.analyze(page);
                log.info("Page {}: Layout analysis completed, {} block(s) detected", page.pageIndex(), blocks.size());

                log.info("Page {}: Starting OCR for all blocks", page.pageIndex());
                List<CompletableFuture<OcrBlockResult>> ocrTasks = blocks.stream()
                        .map(ocrEngine::recognize)
                        .toList();

                List<OcrBlockResult> processed = ocrTasks.stream()
                        .map(CompletableFuture::join)
                        .map(textPostProcessor::process)
                        .filter(block -> !block.text().isBlank())
                        .toList();
                log.info("Page {}: OCR + post-processing completed, {} non-empty block(s) kept",
                        page.pageIndex(), processed.size());

                return new PageModel(page.pageIndex(), page.widthPts(), page.heightPts(), processed);
            } catch (IOException e) {
                log.error("Page {} processing failed", page.pageIndex(), e);
                throw new IllegalStateException("Page processing failed for page " + page.pageIndex(), e);
            }
        }, executor);
    }
}
