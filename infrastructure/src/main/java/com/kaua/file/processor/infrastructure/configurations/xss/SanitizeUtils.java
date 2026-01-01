package com.kaua.file.processor.infrastructure.configurations.xss;

import org.owasp.encoder.Encode;

public final class SanitizeUtils {

    private SanitizeUtils() {}

    public static String sanitize(final String value) {
        return value != null ? Encode.forHtml(value) : null;
    }
}
