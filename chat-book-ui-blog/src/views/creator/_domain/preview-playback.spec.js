import { describe, expect, it } from 'vitest';

import {
    consumePreviewPlaybackTokens,
    resolvePreviewPlaybackBudget,
    tokenizePreviewDelta
} from './preview-playback.js';

describe('preview playback helpers', () => {
    it('tokenizes chinese by character and english by word', () => {
        expect(tokenizePreviewDelta('你好，hello world!\n下一行')).toEqual([
            '你',
            '好',
            '，',
            'hello',
            ' ',
            'world',
            '!',
            '\n',
            '下',
            '一',
            '行'
        ]);
    });

    it('uses a larger playback budget when backlog grows', () => {
        expect(resolvePreviewPlaybackBudget(10)).toEqual({ maxTokens: 2, maxChars: 10 });
        expect(resolvePreviewPlaybackBudget(48)).toEqual({ maxTokens: 4, maxChars: 16 });
        expect(resolvePreviewPlaybackBudget(80)).toEqual({ maxTokens: 8, maxChars: 24 });
    });

    it('consumes a bounded slice without emptying the full queue at once', () => {
        const queue = Array.from('你好世界马上输出');

        const slice = consumePreviewPlaybackTokens(queue);

        expect(slice).toBe('你好');
        expect(queue).toEqual(['世', '界', '马', '上', '输', '出']);
    });

    it('respects the character budget for longer english tokens', () => {
        const queue = ['alphabet', 'beta', 'gamma'];

        const slice = consumePreviewPlaybackTokens(queue, { maxTokens: 2, maxChars: 10 });

        expect(slice).toBe('alphabet');
        expect(queue).toEqual(['beta', 'gamma']);
    });
});
