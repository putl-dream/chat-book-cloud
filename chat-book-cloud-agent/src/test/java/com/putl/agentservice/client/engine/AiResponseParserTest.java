package com.putl.agentservice.client.engine;

import com.putl.agentservice.constants.AgentMessageTypeConstants;
import com.putl.agentservice.model.vo.AgentAssistantMessage;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiResponseParserTest {

    private final AiResponseParser parser = new AiResponseParser();

    @Test
    void parseJsonObjectShouldStripCodeFence() {
        ArticleDraftResult result = parser.parseJsonObject("""
                ```json
                {"title":"标题","summary":"摘要","content":"正文"}
                ```
                """, ArticleDraftResult.class);

        assertEquals("标题", result.getTitle());
        assertEquals("摘要", result.getSummary());
        assertEquals("正文", result.getContent());
    }

    @Test
    void parseStructuredChatShouldFallbackToTextWhenJsonCannotBeParsed() {
        AgentAssistantMessage message = parser.parseStructuredChat("这是一段普通文本回复");

        assertEquals(AgentMessageTypeConstants.TEXT, message.getMessageType());
        assertEquals("这是一段普通文本回复", message.getContent());
        assertNull(message.getPayload());
    }

    @Test
    void parseStructuredChatShouldReadFinalJsonFromEnvelope() {
        AgentAssistantMessage message = parser.parseStructuredChat("""
                <agent_preview>先看受众</agent_preview>
                <agent_final>{"messageType":"text","content":"先看受众","payload":null}</agent_final>
                """);

        assertEquals(AgentMessageTypeConstants.TEXT, message.getMessageType());
        assertEquals("先看受众", message.getContent());
        assertNull(message.getPayload());
    }

    @Test
    void parseStructuredChatShouldFallbackToEnvelopePreviewWhenFinalJsonIsInvalid() {
        AgentAssistantMessage message = parser.parseStructuredChat("""
                <agent_preview>先看受众</agent_preview>
                <agent_final>{invalid-json}</agent_final>
                """);

        assertEquals(AgentMessageTypeConstants.TEXT, message.getMessageType());
        assertEquals("先看受众", message.getContent());
        assertNull(message.getPayload());
    }

    @Test
    void parseStructuredChatShouldNormalizeInteractiveFormPayload() {
        AgentAssistantMessage message = parser.parseStructuredChat("""
                {
                  "messageType": "interactive_form",
                  "content": "请补充信息",
                  "payload": {
                    "title": "补充资料",
                    "questions": [
                      {
                        "label": "目标读者",
                        "type": "single_choice",
                        "options": [
                          {
                            "value": "Java 开发者"
                          }
                        ]
                      }
                    ]
                  }
                }
                """);

        assertEquals(AgentMessageTypeConstants.INTERACTIVE_FORM, message.getMessageType());
        assertEquals("请补充信息", message.getContent());
        assertNotNull(message.getPayload());
        assertEquals("batch", message.getPayload().getSubmitMode());
        assertTrue(message.getPayload().getFormId().startsWith("form_"));
        assertEquals(1, message.getPayload().getQuestions().size());
        assertEquals("question_1", message.getPayload().getQuestions().get(0).getId());
        assertEquals("Java 开发者", message.getPayload().getQuestions().get(0).getOptions().get(0).getLabel());
        assertEquals("Java 开发者", message.getPayload().getQuestions().get(0).getOptions().get(0).getValue());
    }

    @Test
    void parseJsonObjectShouldFailWhenPayloadIsInvalid() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> parser.parseJsonObject("{invalid-json}", ArticleDraftResult.class));

        assertTrue(exception.getMessage().contains("ArticleDraftResult"));
    }
}
