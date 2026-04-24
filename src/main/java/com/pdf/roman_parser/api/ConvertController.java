package com.pdf.roman_parser.api;

import com.pdf.roman_parser.service.PdfToDocxService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/convert")
@Validated
public class ConvertController {

    private static final long MAX_BYTES = 50L * 1024 * 1024;

    private final PdfToDocxService service;
    public ConvertController(PdfToDocxService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convert(@RequestPart("file") @NotNull MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (file.getSize() > MAX_BYTES) {
            return ResponseEntity.status(413).build();
        }

        byte[] pdfBytes = file.getBytes();
        byte[] docx = service.convert(pdfBytes).get(5, TimeUnit.MINUTES);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("converted.docx").build());
        return ResponseEntity.ok().headers(headers).body(docx);
    }
}
