package epu.aeshop.util;

import java.text.Normalizer;

public class TextAnalyzer {
    public static String preprocess(CharSequence s) {
        return s == null? null: Normalizer.normalize(s, Normalizer.Form.NFC).toLowerCase();
    }
}
