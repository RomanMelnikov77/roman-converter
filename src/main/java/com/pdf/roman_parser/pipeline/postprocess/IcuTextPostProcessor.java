package com.pdf.roman_parser.pipeline.postprocess;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.pdf.roman_parser.pipeline.model.OcrBlockResult;
import org.springframework.stereotype.Component;

@Component
public class IcuTextPostProcessor implements TextPostProcessor {

    @Override
    public OcrBlockResult process(OcrBlockResult result) {
        String text = result.text() == null ? "" : result.text().trim();
        if (text.isEmpty()) {
            return result;
        }

        Bidi bidi = new Bidi(text, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        String reordered = bidi.writeReordered(Bidi.DO_MIRRORING);
        byte baseDirection = Bidi.getBaseDirection(text);
        boolean rtl = baseDirection == Bidi.RTL;

        String shaped = reordered;
        if (containsArabic(reordered)) {
            try {
                ArabicShaping shaping = new ArabicShaping(ArabicShaping.LETTERS_SHAPE);
                shaped = shaping.shape(reordered);
                rtl = true;
            } catch (ArabicShapingException ignored) {
            }
        }

        return new OcrBlockResult(result.pageIndex(), result.boundingBox(), shaped, rtl);
    }

    private boolean containsArabic(String value) {
        for (int i = 0; i < value.length(); i++) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(value.charAt(i));
            if (block == Character.UnicodeBlock.ARABIC
                    || block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A
                    || block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B) {
                return true;
            }
        }
        return false;
    }
}
