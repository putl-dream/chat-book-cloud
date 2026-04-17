package com.putl.agentservice.service.impl;

final class StructuredMessageStreamPreviewExtractor {

    private StructuredMessageStreamPreviewExtractor() {
    }

    static String extractContent(String buffer) {
        return extractField(buffer, "content");
    }

    private static String extractField(String buffer, String fieldName) {
        if (buffer == null || buffer.isEmpty() || fieldName == null || fieldName.isEmpty()) {
            return "";
        }

        String marker = "\"" + fieldName + "\"";
        int fieldIndex = buffer.indexOf(marker);
        if (fieldIndex < 0) {
            return "";
        }

        int colonIndex = buffer.indexOf(':', fieldIndex + marker.length());
        if (colonIndex < 0) {
            return "";
        }

        int quoteIndex = skipWhitespace(buffer, colonIndex + 1);
        if (quoteIndex < 0 || quoteIndex >= buffer.length() || buffer.charAt(quoteIndex) != '"') {
            return "";
        }

        StringBuilder rawValue = new StringBuilder();
        int consecutiveBackslashes = 0;
        for (int cursor = quoteIndex + 1; cursor < buffer.length(); cursor += 1) {
            char current = buffer.charAt(cursor);
            if (current == '"' && consecutiveBackslashes % 2 == 0) {
                return decodeJsonStringFragment(rawValue.toString());
            }
            rawValue.append(current);
            consecutiveBackslashes = current == '\\' ? consecutiveBackslashes + 1 : 0;
        }
        return decodeJsonStringFragment(rawValue.toString());
    }

    private static int skipWhitespace(String value, int startIndex) {
        for (int index = Math.max(0, startIndex); index < value.length(); index += 1) {
            if (!Character.isWhitespace(value.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static String decodeJsonStringFragment(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        return decodeJsonStringPrefix(value);
    }

    private static String decodeJsonStringPrefix(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index += 1) {
            char current = value.charAt(index);
            if (current != '\\') {
                builder.append(current);
                continue;
            }
            if (index + 1 >= value.length()) {
                break;
            }

            char next = value.charAt(++index);
            switch (next) {
                case '"', '\\', '/' -> builder.append(next);
                case 'b' -> builder.append('\b');
                case 'f' -> builder.append('\f');
                case 'n' -> builder.append('\n');
                case 'r' -> builder.append('\r');
                case 't' -> builder.append('\t');
                case 'u' -> {
                    if (index + 4 >= value.length()) {
                        return builder.toString();
                    }
                    String hex = value.substring(index + 1, index + 5);
                    try {
                        builder.append((char) Integer.parseInt(hex, 16));
                    } catch (NumberFormatException ignored) {
                        return builder.toString();
                    }
                    index += 4;
                }
                default -> {
                    return builder.toString();
                }
            }
        }
        return builder.toString();
    }
}
