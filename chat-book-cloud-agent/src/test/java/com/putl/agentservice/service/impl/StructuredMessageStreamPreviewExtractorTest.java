package com.putl.agentservice.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructuredMessageStreamPreviewExtractorTest {

    @Test
    void shouldExtractPartialContentWhileJsonStringIsStillOpen() {
        String preview = StructuredMessageStreamPreviewExtractor.extractContent(
                "{\"messageType\":\"text\",\"content\":\"第一段正在生成"
        );

        assertEquals("第一段正在生成", preview);
    }

    @Test
    void shouldKeepDecodedPrefixWhenChunkEndsInsideEscapeSequence() {
        String preview = StructuredMessageStreamPreviewExtractor.extractContent(
                "{\"messageType\":\"text\",\"content\":\"第一行\\n第二"
        );

        assertEquals("第一行\n第二", preview);
    }

    @Test
    void shouldStopAtIncompleteUnicodeEscapeWithoutDroppingExistingText() {
        String preview = StructuredMessageStreamPreviewExtractor.extractContent(
                "{\"messageType\":\"text\",\"content\":\"已生成\\u4F"
        );

        assertEquals("已生成", preview);
    }
}
