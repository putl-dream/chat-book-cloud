package com.putl.agentservice.client.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructuredChatOutputFormatTest {

    @Test
    void extractPreviewShouldReturnPartialPreviewBeforeCloseTagArrives() {
        String preview = StructuredChatOutputFormat.extractPreview("<agent_preview>先看受众");

        assertEquals("先看受众", preview);
    }

    @Test
    void extractFinalPayloadShouldReturnWrappedJsonOnly() {
        String payload = StructuredChatOutputFormat.extractFinalPayload("""
                <agent_preview>先看受众</agent_preview>
                <agent_final>{"messageType":"text","content":"先看受众","payload":null}</agent_final>
                """);

        assertEquals("{\"messageType\":\"text\",\"content\":\"先看受众\",\"payload\":null}", payload);
    }
}
