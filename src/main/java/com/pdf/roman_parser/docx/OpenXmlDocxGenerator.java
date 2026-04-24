package com.pdf.roman_parser.docx;

import com.pdf.roman_parser.pipeline.model.DocumentModel;
import com.pdf.roman_parser.pipeline.model.OcrBlockResult;
import com.pdf.roman_parser.pipeline.model.PageModel;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class OpenXmlDocxGenerator implements DocxGenerator {

    @Override
    public byte[] generate(DocumentModel model) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {

            put(zip, "[Content_Types].xml", contentTypes());
            put(zip, "_rels/.rels", rootRels());
            put(zip, "word/_rels/document.xml.rels", documentRels());
            put(zip, "word/document.xml", documentXml(model));

            zip.finish();
            return bos.toByteArray();
        }
    }

    private void put(ZipOutputStream zip, String path, String data) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(data.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String documentXml(DocumentModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">")
                .append("<w:body>");

        for (int i = 0; i < model.pages().size(); i++) {
            PageModel page = model.pages().get(i);
            appendPageBlocks(sb, page);
            if (i < model.pages().size() - 1) {
                sb.append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>");
            }
        }

        sb.append("<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/></w:sectPr>")
                .append("</w:body></w:document>");
        return sb.toString();
    }

    private void appendPageBlocks(StringBuilder sb, PageModel page) {
        for (OcrBlockResult block : page.blocks()) {
            long topTwips = (long) Math.round(block.boundingBox().y() * 15d);
            sb.append("<w:p>")
                    .append("<w:pPr><w:spacing w:before=\"").append(topTwips).append("\"/></w:pPr>")
                    .append("<w:r>");
            if (block.rtl()) {
                sb.append("<w:rPr><w:rtl/></w:rPr>");
            }
            sb.append("<w:t xml:space=\"preserve\">")
                    .append(escape(block.text()))
                    .append("</w:t>")
                    .append("</w:r></w:p>");
        }
    }

    private String escape(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
                "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
                "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
                "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>" +
                "</Types>";
    }

    private String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>" +
                "</Relationships>";
    }

    private String documentRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"></Relationships>";
    }
}
