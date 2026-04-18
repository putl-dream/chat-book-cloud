export const PREVIEW_PLAYBACK_INTERVAL_MS = 24;

const ASCII_WORD_PATTERN = /[A-Za-z0-9]/;
const WHITESPACE_PATTERN = /\s/;

export function tokenizePreviewDelta(delta = '') {
    const tokens = [];
    let asciiBuffer = '';
    let whitespaceBuffer = '';

    const flushAsciiBuffer = () => {
        if (!asciiBuffer) {
            return;
        }
        tokens.push(asciiBuffer);
        asciiBuffer = '';
    };

    const flushWhitespaceBuffer = () => {
        if (!whitespaceBuffer) {
            return;
        }
        tokens.push(whitespaceBuffer);
        whitespaceBuffer = '';
    };

    for (const char of String(delta || '')) {
        if (WHITESPACE_PATTERN.test(char)) {
            flushAsciiBuffer();
            whitespaceBuffer += char;
            continue;
        }

        flushWhitespaceBuffer();
        if (ASCII_WORD_PATTERN.test(char)) {
            asciiBuffer += char;
            continue;
        }

        flushAsciiBuffer();
        tokens.push(char);
    }

    flushAsciiBuffer();
    flushWhitespaceBuffer();
    return tokens;
}

export function resolvePreviewPlaybackBudget(queueLength = 0) {
    if (queueLength > 120) {
        return { maxTokens: 12, maxChars: 36 };
    }
    if (queueLength > 60) {
        return { maxTokens: 8, maxChars: 24 };
    }
    if (queueLength > 24) {
        return { maxTokens: 4, maxChars: 16 };
    }
    return { maxTokens: 2, maxChars: 10 };
}

export function consumePreviewPlaybackTokens(queue = [], budget = resolvePreviewPlaybackBudget(queue.length)) {
    if (!Array.isArray(queue) || queue.length === 0) {
        return '';
    }

    const nextBudget = budget || resolvePreviewPlaybackBudget(queue.length);
    let delta = '';
    let consumedTokens = 0;
    let consumedChars = 0;

    while (queue.length > 0 && consumedTokens < nextBudget.maxTokens) {
        const nextToken = String(queue[0] ?? '');
        const nextChars = nextToken.length;
        if (delta && consumedChars + nextChars > nextBudget.maxChars) {
            break;
        }
        delta += queue.shift();
        consumedTokens += 1;
        consumedChars += nextChars;
    }

    if (!delta && queue.length > 0) {
        delta = String(queue.shift() ?? '');
    }
    return delta;
}
