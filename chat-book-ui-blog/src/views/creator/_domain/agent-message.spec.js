import { describe, expect, it } from 'vitest';

import { resolveCompletedStreamingMessage } from './agent-message.js';

describe('agent-message', () => {
    it('preserves the optimistic assistant id when completion payload arrives', () => {
        const result = resolveCompletedStreamingMessage({
            id: 'assistant-optimistic-1',
            role: 'assistant',
            messageType: 'text',
            content: '',
            streaming: true
        }, {
            finalMessage: {
                id: 88,
                role: 'ASSISTANT',
                messageType: 'text',
                content: '最终回复'
            }
        }, '最终回复');

        expect(result.id).toBe('assistant-optimistic-1');
        expect(result.content).toBe('最终回复');
    });

    it('keeps the streamed preview when completion telemetry requests preview fallback', () => {
        const result = resolveCompletedStreamingMessage({
            id: 'assistant-optimistic-2',
            role: 'assistant',
            messageType: 'text',
            content: '',
            streaming: true
        }, {
            finalMessage: {
                id: 99,
                role: 'ASSISTANT',
                messageType: 'text',
                content: ''
            },
            telemetry: {
                previewFallbackApplied: true
            }
        }, '预览里的完整文本');

        expect(result.id).toBe('assistant-optimistic-2');
        expect(result.content).toBe('预览里的完整文本');
    });
});
