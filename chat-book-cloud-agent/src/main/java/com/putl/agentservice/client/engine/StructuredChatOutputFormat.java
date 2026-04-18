package com.putl.agentservice.client.engine;

public final class StructuredChatOutputFormat {

    public static final String PREVIEW_OPEN_TAG = "<agent_preview>";
    public static final String PREVIEW_CLOSE_TAG = "</agent_preview>";
    public static final String FINAL_OPEN_TAG = "<agent_final>";
    public static final String FINAL_CLOSE_TAG = "</agent_final>";

    private StructuredChatOutputFormat() {
    }

    public static String extractPreview(String rawText) {
        return extractSection(rawText, PREVIEW_OPEN_TAG, PREVIEW_CLOSE_TAG, true);
    }

    public static String extractFinalPayload(String rawText) {
        return extractSection(rawText, FINAL_OPEN_TAG, FINAL_CLOSE_TAG, false);
    }

    public static String extractPartialFinalPayload(String rawText) {
        return extractSection(rawText, FINAL_OPEN_TAG, FINAL_CLOSE_TAG, true);
    }

    public static boolean containsEnvelope(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return false;
        }
        return rawText.contains(PREVIEW_OPEN_TAG) || rawText.contains(FINAL_OPEN_TAG);
    }

    private static String extractSection(String rawText,
                                         String openTag,
                                         String closeTag,
                                         boolean allowPartial) {
        if (rawText == null || rawText.isEmpty()) {
            return "";
        }

        int openIndex = rawText.indexOf(openTag);
        if (openIndex < 0) {
            return "";
        }

        int contentStart = openIndex + openTag.length();
        int closeIndex = rawText.indexOf(closeTag, contentStart);
        if (closeIndex < 0) {
            return allowPartial ? rawText.substring(contentStart) : "";
        }
        return rawText.substring(contentStart, closeIndex);
    }
}
