package com.pdf.roman_parser.pipeline.ocr;

import com.pdf.roman_parser.pipeline.model.LayoutBlock;
import com.pdf.roman_parser.pipeline.model.OcrBlockResult;

import java.util.concurrent.CompletableFuture;

public interface OcrEngine {
    CompletableFuture<OcrBlockResult> recognize(LayoutBlock block);
}
