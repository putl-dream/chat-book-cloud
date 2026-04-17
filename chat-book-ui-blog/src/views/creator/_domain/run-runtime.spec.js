import { describe, expect, it } from 'vitest';

import {
    AGENT_RUN_KIND,
    AGENT_RUN_STATUS,
    AGENT_RUNTIME_EVENT,
    appendArtifactDelta,
    appendMessagePreviewDelta,
    completeArtifactRun,
    createRunRuntime,
    startArtifactRun,
    startMessageRun
} from './run-runtime.js';

describe('run-runtime', () => {
    it('exposes generic runtime event names directly', () => {
        expect(AGENT_RUNTIME_EVENT.MESSAGE_DELTA).toBe('message.delta');
        expect(AGENT_RUNTIME_EVENT.ARTIFACT_COMPLETED).toBe('artifact.completed');
    });

    it('accumulates chat preview text and delta counters', () => {
        let runtime = createRunRuntime({ runKind: AGENT_RUN_KIND.CHAT });
        runtime = startMessageRun(runtime, {
            runId: 'chat-1',
            messageId: 'assistant-1',
            sessionId: 12
        });
        runtime = appendMessagePreviewDelta(runtime, '你好');
        runtime = appendMessagePreviewDelta(runtime, '，世界');

        expect(runtime.status).toBe(AGENT_RUN_STATUS.RUNNING);
        expect(runtime.previewText).toBe('你好，世界');
        expect(runtime.previewParts).toEqual([{ type: 'text', content: '你好，世界' }]);
        expect(runtime.meta.deltaCount).toBe(2);
    });

    it('builds artifact preview from incremental chunks', () => {
        let runtime = createRunRuntime({ runKind: AGENT_RUN_KIND.DRAFT });
        runtime = startArtifactRun(runtime, {
            runId: 'draft-1',
            sessionId: 88,
            statusText: '生成中'
        });
        runtime = appendArtifactDelta(runtime, {
            chunk: '{"title":"标题","content":"正文"}',
            buildPreview: () => ({
                title: '标题',
                content: '正文'
            })
        });
        runtime = completeArtifactRun(runtime, {
            finalArtifact: {
                title: '标题',
                content: '正文'
            },
            statusText: '已完成'
        });

        expect(runtime.artifactBuffer).toContain('"title":"标题"');
        expect(runtime.artifactPreview?.title).toBe('标题');
        expect(runtime.finalArtifact?.content).toBe('正文');
        expect(runtime.status).toBe(AGENT_RUN_STATUS.COMPLETED);
    });
});
