<template>
    <div class="streaming-message-renderer">
        <template v-for="(block, blockIndex) in blocks" :key="`block-${blockIndex}`">
            <p v-if="block.type === 'paragraph'" class="streaming-message-renderer__paragraph">
                <template v-for="(token, tokenIndex) in block.tokens" :key="`paragraph-${blockIndex}-${tokenIndex}`">
                    <code v-if="token.type === 'code'" class="streaming-message-renderer__inline-code">{{ token.content }}</code>
                    <span v-else class="streaming-message-renderer__text">{{ token.content }}</span>
                </template>
            </p>

            <ul v-else-if="block.type === 'unordered_list'" class="streaming-message-renderer__list">
                <li
                    v-for="(item, itemIndex) in block.items"
                    :key="`unordered-${blockIndex}-${itemIndex}`"
                    class="streaming-message-renderer__list-item"
                >
                    <template v-for="(token, tokenIndex) in item.tokens" :key="`unordered-${blockIndex}-${itemIndex}-${tokenIndex}`">
                        <code v-if="token.type === 'code'" class="streaming-message-renderer__inline-code">{{ token.content }}</code>
                        <span v-else class="streaming-message-renderer__text">{{ token.content }}</span>
                    </template>
                </li>
            </ul>

            <ol v-else-if="block.type === 'ordered_list'" class="streaming-message-renderer__list streaming-message-renderer__list--ordered">
                <li
                    v-for="(item, itemIndex) in block.items"
                    :key="`ordered-${blockIndex}-${itemIndex}`"
                    class="streaming-message-renderer__list-item"
                >
                    <template v-for="(token, tokenIndex) in item.tokens" :key="`ordered-${blockIndex}-${itemIndex}-${tokenIndex}`">
                        <code v-if="token.type === 'code'" class="streaming-message-renderer__inline-code">{{ token.content }}</code>
                        <span v-else class="streaming-message-renderer__text">{{ token.content }}</span>
                    </template>
                </li>
            </ol>

            <pre v-else-if="block.type === 'code'" class="streaming-message-renderer__code-block"><code>{{ block.content }}</code></pre>
        </template>
    </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
    content: {
        type: String,
        default: ''
    }
});

function tokenizeInline(value = '') {
    if (!value) {
        return [];
    }

    const tokens = [];
    let cursor = 0;
    while (cursor < value.length) {
        const start = value.indexOf('`', cursor);
        if (start < 0) {
            tokens.push({ type: 'text', content: value.slice(cursor) });
            break;
        }

        if (start > cursor) {
            tokens.push({ type: 'text', content: value.slice(cursor, start) });
        }

        const end = value.indexOf('`', start + 1);
        if (end < 0) {
            tokens.push({ type: 'text', content: value.slice(start) });
            break;
        }

        const code = value.slice(start + 1, end);
        if (code) {
            tokens.push({ type: 'code', content: code });
        } else {
            tokens.push({ type: 'text', content: '``' });
        }
        cursor = end + 1;
    }

    return tokens;
}

function createParagraph(lines = []) {
    const content = lines.join('\n').trim();
    if (!content) {
        return null;
    }
    return {
        type: 'paragraph',
        tokens: tokenizeInline(content)
    };
}

function createListItem(content = '') {
    return {
        tokens: tokenizeInline(content)
    };
}

function flushParagraph(buffer, blocks) {
    const paragraph = createParagraph(buffer.lines);
    if (paragraph) {
        blocks.push(paragraph);
    }
    buffer.lines = [];
}

function flushList(buffer, blocks) {
    if (buffer.items.length === 0) {
        return;
    }
    blocks.push({
        type: buffer.ordered ? 'ordered_list' : 'unordered_list',
        items: [...buffer.items]
    });
    buffer.ordered = false;
    buffer.items = [];
}

function parseStreamingBlocks(content = '') {
    const blocks = [];
    const paragraphBuffer = { lines: [] };
    const listBuffer = { ordered: false, items: [] };
    const codeBuffer = [];
    let inCodeFence = false;

    const lines = String(content || '').replace(/\r\n/g, '\n').split('\n');
    lines.forEach((line) => {
        const trimmed = line.trim();
        if (trimmed.startsWith('```')) {
            flushParagraph(paragraphBuffer, blocks);
            flushList(listBuffer, blocks);
            if (inCodeFence) {
                blocks.push({
                    type: 'code',
                    content: codeBuffer.join('\n')
                });
                codeBuffer.length = 0;
                inCodeFence = false;
                return;
            }
            inCodeFence = true;
            return;
        }

        if (inCodeFence) {
            codeBuffer.push(line);
            return;
        }

        const listMatch = line.match(/^(\s*)([-*+]|\d+\.)\s+(.*)$/);
        if (listMatch) {
            flushParagraph(paragraphBuffer, blocks);
            const ordered = /\d+\./.test(listMatch[2]);
            if (listBuffer.items.length > 0 && listBuffer.ordered !== ordered) {
                flushList(listBuffer, blocks);
            }
            listBuffer.ordered = ordered;
            listBuffer.items.push(createListItem(listMatch[3]));
            return;
        }

        if (!trimmed) {
            flushParagraph(paragraphBuffer, blocks);
            flushList(listBuffer, blocks);
            return;
        }

        flushList(listBuffer, blocks);
        paragraphBuffer.lines.push(line);
    });

    if (inCodeFence) {
        blocks.push({
            type: 'code',
            content: codeBuffer.join('\n')
        });
    }

    flushParagraph(paragraphBuffer, blocks);
    flushList(listBuffer, blocks);
    return blocks;
}

const blocks = computed(() => parseStreamingBlocks(props.content));
</script>

<style scoped>
.streaming-message-renderer {
    width: 100%;
    color: inherit;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.streaming-message-renderer__paragraph,
.streaming-message-renderer__list {
    margin: 0;
    line-height: 1.72;
}

.streaming-message-renderer__paragraph {
    white-space: pre-wrap;
    word-break: break-word;
}

.streaming-message-renderer__list {
    padding-left: 1.3rem;
}

.streaming-message-renderer__list-item {
    line-height: 1.72;
    white-space: pre-wrap;
    word-break: break-word;
}

.streaming-message-renderer__inline-code,
.streaming-message-renderer__code-block {
    font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.streaming-message-renderer__inline-code {
    margin: 0 0.15rem;
    padding: 0.1rem 0.35rem;
    border-radius: 0.45rem;
    background: rgba(19, 39, 63, 0.08);
    font-size: 0.92em;
}

.streaming-message-renderer__code-block {
    margin: 0;
    padding: 0.9rem 1rem;
    border-radius: 16px;
    background: rgba(19, 39, 63, 0.08);
    overflow-x: auto;
    white-space: pre-wrap;
    word-break: break-word;
    line-height: 1.66;
}
</style>
