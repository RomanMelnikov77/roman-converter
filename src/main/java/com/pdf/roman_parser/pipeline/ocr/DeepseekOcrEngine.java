package com.pdf.roman_parser.pipeline.ocr;

import com.pdf.roman_parser.infra.deepseek.DeepseekOcrClient;
import com.pdf.roman_parser.pipeline.model.LayoutBlock;
import com.pdf.roman_parser.pipeline.model.OcrBlockResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class DeepseekOcrEngine implements OcrEngine {

    private final DeepseekOcrClient client;

    public DeepseekOcrEngine(DeepseekOcrClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<OcrBlockResult> recognize(LayoutBlock block) {
        return client.ocr(block.imagePng())
                .onErrorReturn("")
                .map(text -> new OcrBlockResult(
                        block.pageIndex(),
                        block.boundingBox(),
                        text,
                        false
                ))
                .toFuture();
    }
}
