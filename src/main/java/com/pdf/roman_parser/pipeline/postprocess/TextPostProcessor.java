package com.pdf.roman_parser.pipeline.postprocess;

import com.pdf.roman_parser.pipeline.model.OcrBlockResult;

public interface TextPostProcessor {
    OcrBlockResult process(OcrBlockResult result);
}
