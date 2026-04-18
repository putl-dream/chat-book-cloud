<template>
    <div class="ai-sidebar c-editor-panel c-editor-panel--ai">
        <div class="ai-sidebar-header c-editor-panel__header">
            <div class="editor-ai-panel__heading">
                <span class="ai-sidebar-title c-editor-panel__title">AI 编辑助手</span>
                <p class="editor-ai-panel__subtitle">选中一段内容后，直接在对话框里描述你想怎么改；也可以只讨论文章观点和信息缺口。</p>
            </div>
            <el-icon class="close-icon c-editor-panel__close" @click="$emit('close')">
                <Close />
            </el-icon>
        </div>

        <div class="ai-sidebar-body c-editor-panel__body">
            <section class="editor-ai-panel__context">
                <div class="editor-ai-panel__context-card">
                    <span class="editor-ai-panel__context-label">当前上下文</span>
                    <div class="editor-ai-panel__context-meta">
                        <span class="editor-ai-panel__chip">{{ titleDisplay }}</span>
                        <span class="editor-ai-panel__chip">{{ selectionLabel }}</span>
                    </div>
                    <p class="editor-ai-panel__context-text">
                        {{ selectionPreview }}
                    </p>
                </div>
            </section>

            <section ref="messageListRef" class="editor-ai-panel__messages custom-scrollbar">
                <div v-if="messages.length === 0" class="editor-ai-panel__empty">
                    <p class="editor-ai-panel__empty-title">这里就是你的编辑对话区</p>
                    <p class="editor-ai-panel__empty-text">
                        可以直接说“把这段改得更有故事感”“这一节论证还缺什么”“续写一个转场段落”。如果 AI 产出了可应用的正文修改，你再决定要不要应用。
                    </p>
                </div>

                <article
                    v-for="message in messages"
                    :key="message.id"
                    class="editor-ai-panel__message"
                    :class="`is-${message.role}`"
                >
                    <div class="editor-ai-panel__message-bubble">
                        <div class="editor-ai-panel__message-meta">
                            <span>{{ message.role === 'user' ? '你' : 'AI' }}</span>
                            <span v-if="message.targetLabel">{{ message.targetLabel }}</span>
                        </div>
                        <p class="editor-ai-panel__message-text">{{ message.content }}</p>
                        <p
                            v-if="message.resultPreview"
                            class="editor-ai-panel__message-preview"
                        >
                            {{ message.resultPreview }}
                        </p>
                        <div
                            v-if="message.role === 'assistant' && message.canApply && !message.applied"
                            class="editor-ai-panel__message-actions"
                        >
                            <el-button size="small" type="primary" plain @click="handleApplyMessage(message.id)">
                                应用到正文
                            </el-button>
                        </div>
                        <p
                            v-if="message.applied"
                            class="editor-ai-panel__message-applied"
                        >
                            已应用到正文，可用 Ctrl+Z 撤销
                        </p>
                        <p
                            v-if="message.applyError"
                            class="editor-ai-panel__message-error"
                        >
                            {{ message.applyError }}
                        </p>
                    </div>
                </article>
            </section>

            <section class="editor-ai-panel__composer">
                <div class="editor-ai-panel__target-row">
                    <button
                        v-for="option in targetOptions"
                        :key="option.value"
                        class="editor-ai-panel__target-btn"
                        :class="{ 'is-active': applyTarget === option.value }"
                        type="button"
                        :disabled="requesting || disabled || (option.value === APPLY_TARGET.REPLACE_SELECTION && !selectionState.hasSelection)"
                        @click="applyTarget = option.value"
                    >
                        {{ option.label }}
                    </button>
                </div>

                <el-input
                    v-model="inputValue"
                    type="textarea"
                    resize="none"
                    :autosize="{ minRows: 3, maxRows: 8 }"
                    maxlength="1000"
                    :disabled="requesting || disabled || !hasEditor"
                    :placeholder="inputPlaceholder"
                    @keydown.ctrl.enter.prevent="handleSendMessage"
                />

                <div class="editor-ai-panel__composer-footer">
                    <p class="editor-ai-panel__footer-text">
                        {{ footerText }}
                    </p>
                    <el-button
                        type="primary"
                        :loading="requesting"
                        :disabled="disabled || !hasEditor || !inputValue.trim()"
                        @click="handleSendMessage"
                    >
                        发送消息
                    </el-button>
                </div>
            </section>
        </div>
    </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, unref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Close } from '@element-plus/icons-vue';

import { buildRichTextEditorHtml } from '@/components/common/rich-text/content-pipeline.js';
import { assistEditorContent } from '@/views/creator/_domain/agent.js';

const APPLY_TARGET = Object.freeze({
    CHAT_ONLY: 'chat_only',
    INSERT_AT_CURSOR: 'insert_at_cursor',
    REPLACE_SELECTION: 'replace_selection',
    APPEND_TO_END: 'append_to_end'
});

const props = defineProps({
    editor: {
        type: [Object, Function],
        default: null
    },
    title: {
        type: String,
        default: ''
    },
    summary: {
        type: String,
        default: ''
    },
    disabled: {
        type: Boolean,
        default: false
    }
});

defineEmits(['close']);

const editorInstance = computed(() => unref(props.editor));
const hasEditor = computed(() => Boolean(editorInstance.value?.state?.doc));
const requesting = ref(false);
const inputValue = ref('');
const applyTarget = ref(APPLY_TARGET.CHAT_ONLY);
const messageListRef = ref(null);
const messages = ref([]);
const selectionState = ref({
    from: 0,
    to: 0,
    hasSelection: false,
    text: ''
});

const targetOptions = [
    { value: APPLY_TARGET.CHAT_ONLY, label: '仅聊天' },
    { value: APPLY_TARGET.REPLACE_SELECTION, label: '替换选区' },
    { value: APPLY_TARGET.INSERT_AT_CURSOR, label: '插入光标' },
    { value: APPLY_TARGET.APPEND_TO_END, label: '追加末尾' }
];

const titleDisplay = computed(() => {
    const title = String(props.title || '').trim();
    return title ? `标题：${title}` : '标题：未填写';
});

const selectionLabel = computed(() => (
    selectionState.value.hasSelection
        ? `已选中 ${selectionState.value.text.length} 字`
        : '当前无选区'
));

const selectionPreview = computed(() => {
    if (selectionState.value.hasSelection) {
        return createPreview(selectionState.value.text, 96) || '已捕获当前选中文本。';
    }
    return '没有选中任何内容时，AI 会基于全文上下文续写或在光标位置插入结果。';
});

const inputPlaceholder = computed(() => (
    applyTarget.value === APPLY_TARGET.CHAT_ONLY
        ? '例如：这篇文章的第三节论证还有哪些漏洞？'
        : selectionState.value.hasSelection
        ? '例如：把这段改得更口语化，但保留技术细节'
        : '例如：继续往下写一个自然转场；或者描述你希望 AI 如何补全正文'
));

const footerText = computed(() => {
    if (props.disabled) {
        return '当前正文正由 AI 首稿生成占用，完成后再继续局部编辑。';
    }
    if (requesting.value) {
        return '正在组织聊天回复和候选修改...';
    }
    if (applyTarget.value === APPLY_TARGET.REPLACE_SELECTION && !selectionState.value.hasSelection) {
        return '替换选区模式需要你先在正文里选中一段内容。';
    }
    if (applyTarget.value === APPLY_TARGET.CHAT_ONLY) {
        return '当前是纯聊天模式，适合让 AI 点评文章、补充观点或指出信息缺口。';
    }
    return '当前是编辑建议模式。AI 会先在聊天里给出候选修改，再由你决定是否应用到正文。';
});

const syncSelectionState = () => {
    const editor = editorInstance.value;
    if (!editor?.state?.selection || !editor?.state?.doc) {
        selectionState.value = {
            from: 0,
            to: 0,
            hasSelection: false,
            text: ''
        };
        return;
    }

    const { from, to } = editor.state.selection;
    const hasSelection = from !== to;
    const text = hasSelection
        ? editor.state.doc.textBetween(from, to, '\n\n', '\n\n').trim()
        : '';

    selectionState.value = {
        from,
        to,
        hasSelection,
        text
    };

    if (!hasSelection && applyTarget.value === APPLY_TARGET.REPLACE_SELECTION) {
        applyTarget.value = APPLY_TARGET.CHAT_ONLY;
    }
};

const detachEditorListeners = (editor) => {
    if (!editor?.off) {
        return;
    }
    editor.off('selectionUpdate', syncSelectionState);
    editor.off('transaction', syncSelectionState);
};

const attachEditorListeners = (editor) => {
    if (!editor?.on) {
        syncSelectionState();
        return;
    }
    editor.on('selectionUpdate', syncSelectionState);
    editor.on('transaction', syncSelectionState);
    syncSelectionState();
};

const scrollMessagesToBottom = async () => {
    await nextTick();
    if (!messageListRef.value) {
        return;
    }
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
};

const createMessage = (role, content, extras = {}) => ({
    id: `${Date.now()}-${Math.random()}`,
    role,
    content,
    targetLabel: extras.targetLabel || '',
    result: extras.result || '',
    resultPreview: extras.resultPreview || '',
    applyTarget: extras.applyTarget || APPLY_TARGET.CHAT_ONLY,
    selectionSnapshot: extras.selectionSnapshot || null,
    canApply: Boolean(extras.canApply),
    applied: Boolean(extras.applied),
    applyError: extras.applyError || ''
});

const createPreview = (value, maxLength = 120) => {
    const normalized = String(value || '').trim().replace(/\s+/g, ' ');
    if (!normalized) {
        return '';
    }
    return normalized.length > maxLength
        ? `${normalized.slice(0, maxLength)}...`
        : normalized;
};

const normalizeApplyTarget = (value) => {
    if (value === APPLY_TARGET.CHAT_ONLY) {
        return 'CHAT_ONLY';
    }
    if (value === APPLY_TARGET.REPLACE_SELECTION) {
        return 'REPLACE_SELECTION';
    }
    if (value === APPLY_TARGET.APPEND_TO_END) {
        return 'APPEND_TO_END';
    }
    return 'INSERT_AT_CURSOR';
};

const resolveTargetLabel = (value) => {
    return targetOptions.find((option) => option.value === value)?.label || '';
};

const resolveModeForTarget = (target) => {
    if (target === APPLY_TARGET.CHAT_ONLY) {
        return 'CHAT';
    }
    if (target === APPLY_TARGET.INSERT_AT_CURSOR || target === APPLY_TARGET.APPEND_TO_END) {
        return 'CONTINUE';
    }
    return 'EDIT';
};

const getEditorPlainText = () => {
    const editor = editorInstance.value;
    if (!editor?.state?.doc) {
        return '';
    }
    if (typeof editor.getText === 'function') {
        const text = editor.getText({ blockSeparator: '\n\n' });
        if (typeof text === 'string' && text.trim()) {
            return text;
        }
    }
    return editor.state.doc.textBetween(0, editor.state.doc.content.size, '\n\n', '\n\n');
};

const serializeHistory = (limit = 6) => {
    return messages.value
        .slice(-limit)
        .map((message) => ({
            role: message.role,
            content: message.content
        }));
};

const applyResultToEditor = (markdown, target, selectionSnapshot) => {
    const editor = editorInstance.value;
    if (!editor?.state?.doc) {
        return false;
    }

    if (target === APPLY_TARGET.CHAT_ONLY) {
        return false;
    }

    const html = buildRichTextEditorHtml(markdown || '', 'markdown');
    if (!html) {
        return false;
    }

    if (target === APPLY_TARGET.REPLACE_SELECTION && selectionSnapshot.hasSelection) {
        const currentSelection = editor.state.selection;
        const currentSelectedText = currentSelection?.from !== currentSelection?.to
            ? editor.state.doc.textBetween(currentSelection.from, currentSelection.to, '\n\n', '\n\n').trim()
            : '';
        if (!currentSelectedText || currentSelectedText !== selectionSnapshot.text) {
            return false;
        }
        return editor.chain().focus().insertContentAt({
            from: currentSelection.from,
            to: currentSelection.to
        }, html).run();
    }

    if (target === APPLY_TARGET.APPEND_TO_END) {
        const endPosition = editor.state.doc.content.size;
        return editor.chain().focus().insertContentAt(endPosition, html).run();
    }

    const currentCursor = editor.state.selection?.to ?? selectionSnapshot.to;
    return editor.chain().focus().insertContentAt({
        from: currentCursor,
        to: currentCursor
    }, html).run();
};

const handleApplyMessage = (messageId) => {
    const messageIndex = messages.value.findIndex((message) => message.id === messageId);
    if (messageIndex < 0) {
        return;
    }

    const message = messages.value[messageIndex];
    if (!message?.canApply || !message?.result) {
        return;
    }

    const applied = applyResultToEditor(
        message.result,
        message.applyTarget,
        message.selectionSnapshot || selectionState.value
    );

    if (!applied) {
        messages.value[messageIndex] = {
            ...message,
            applyError: message.applyTarget === APPLY_TARGET.REPLACE_SELECTION
                ? '选区已经变化，请重新选中对应内容后重新发起这次修改。'
                : '这次候选修改没能成功应用到正文，请再试一次。'
        };
        ElMessage.warning(messages.value[messageIndex].applyError);
        return;
    }

    messages.value[messageIndex] = {
        ...message,
        applied: true,
        applyError: ''
    };
    ElMessage.success('已应用到正文');
};

const runAssist = async ({ mode, instruction, target }) => {
    if (requesting.value || props.disabled) {
        return;
    }
    if (!hasEditor.value) {
        ElMessage.warning('编辑器还没有准备好，请稍后再试');
        return;
    }

    const selectionSnapshot = { ...selectionState.value };
    if (target === APPLY_TARGET.REPLACE_SELECTION && !selectionSnapshot.hasSelection) {
        ElMessage.warning('请先选中一段正文，再执行替换改写');
        return;
    }

    const fullContent = getEditorPlainText().trim();
    if (!fullContent && mode === 'EDIT' && !selectionSnapshot.hasSelection) {
        ElMessage.warning('请先输入一些正文，再让 AI 帮你改写');
        return;
    }

    const finalInstruction = String(instruction || '').trim();
    const userMessage = finalInstruction;

    messages.value.push(createMessage('user', userMessage, {
        targetLabel: resolveTargetLabel(target)
    }));
    await scrollMessagesToBottom();

    requesting.value = true;
    try {
        const response = await assistEditorContent({
            mode,
            applyTarget: normalizeApplyTarget(target),
            instruction: finalInstruction,
            title: props.title,
            summary: props.summary,
            content: fullContent,
            selectedText: selectionSnapshot.text,
            history: serializeHistory()
        });
        const resultMarkdown = String(response?.result || '').trim();
        const replyText = String(response?.reply || '').trim()
            || (resultMarkdown ? '我给出了一版候选修改，你可以决定是否应用到正文。' : '我先从当前文章角度给你一点建议。');

        messages.value.push(createMessage(
            'assistant',
            replyText,
            {
                targetLabel: resolveTargetLabel(target),
                result: resultMarkdown,
                resultPreview: createPreview(resultMarkdown),
                applyTarget: target,
                selectionSnapshot,
                canApply: target !== APPLY_TARGET.CHAT_ONLY && Boolean(resultMarkdown)
            }
        ));
        inputValue.value = '';
        if (resultMarkdown) {
            ElMessage.success('AI 已生成候选修改');
        }
    } catch (error) {
        console.error('Failed to assist editor content:', error);
        messages.value.push(createMessage(
            'assistant',
            error?.message || '这次生成失败了，请换一种说法再试一次。'
        ));
        ElMessage.error(error?.message || 'AI 编辑失败，请稍后重试');
    } finally {
        requesting.value = false;
        await scrollMessagesToBottom();
        syncSelectionState();
    }
};

const handleSendMessage = () => runAssist({
    mode: resolveModeForTarget(applyTarget.value),
    instruction: inputValue.value.trim(),
    target: applyTarget.value
});

watch(editorInstance, (nextEditor, prevEditor) => {
    detachEditorListeners(prevEditor);
    attachEditorListeners(nextEditor);
}, { immediate: true });

watch(() => messages.value.length, () => {
    scrollMessagesToBottom();
});

onBeforeUnmount(() => {
    detachEditorListeners(editorInstance.value);
});
</script>

<style scoped>
.editor-ai-panel__heading {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: 4px;
}

.editor-ai-panel__subtitle {
    margin: 0;
    color: rgba(19, 39, 63, 0.58);
    font-size: 12px;
    line-height: 1.5;
}

.editor-ai-panel__context {
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.editor-ai-panel__context-card {
    padding: 16px;
    border-radius: 18px;
    background: rgba(255, 255, 255, 0.88);
    border: 1px solid rgba(19, 39, 63, 0.08);
    box-shadow: 0 12px 30px rgba(19, 39, 63, 0.06);
}

.editor-ai-panel__context-label {
    display: inline-flex;
    margin-bottom: 10px;
    color: #d1603d;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
}

.editor-ai-panel__context-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 10px;
}

.editor-ai-panel__chip {
    display: inline-flex;
    align-items: center;
    padding: 6px 10px;
    border-radius: 999px;
    background: rgba(19, 39, 63, 0.07);
    color: #13273f;
    font-size: 12px;
    font-weight: 600;
}

.editor-ai-panel__context-text {
    margin: 0;
    color: rgba(19, 39, 63, 0.66);
    font-size: 13px;
    line-height: 1.7;
}

.editor-ai-panel__target-btn:disabled {
    opacity: 0.48;
    cursor: not-allowed;
    box-shadow: none;
}

.editor-ai-panel__messages {
    margin-top: 18px;
    margin-bottom: 18px;
    display: flex;
    min-height: 220px;
    max-height: 44vh;
    flex-direction: column;
    gap: 12px;
    overflow-y: auto;
    padding-right: 4px;
}

.editor-ai-panel__empty {
    padding: 18px 16px;
    border-radius: 18px;
    border: 1px dashed rgba(19, 39, 63, 0.12);
    background: rgba(255, 255, 255, 0.72);
}

.editor-ai-panel__empty-title,
.editor-ai-panel__empty-text,
.editor-ai-panel__message-text,
.editor-ai-panel__message-preview,
.editor-ai-panel__message-applied {
    margin: 0;
}

.editor-ai-panel__empty-title {
    color: #13273f;
    font-size: 14px;
    font-weight: 700;
}

.editor-ai-panel__empty-text {
    margin-top: 8px;
    color: rgba(19, 39, 63, 0.62);
    font-size: 13px;
    line-height: 1.7;
}

.editor-ai-panel__message {
    display: flex;
}

.editor-ai-panel__message.is-user {
    justify-content: flex-end;
}

.editor-ai-panel__message-bubble {
    width: min(100%, 280px);
    padding: 14px 14px 12px;
    border-radius: 18px;
    box-shadow: 0 12px 24px rgba(19, 39, 63, 0.06);
}

.editor-ai-panel__message.is-user .editor-ai-panel__message-bubble {
    background: linear-gradient(135deg, #13273f, #1e3b58);
    color: #fff;
    border-top-right-radius: 6px;
}

.editor-ai-panel__message.is-assistant .editor-ai-panel__message-bubble {
    background: rgba(255, 255, 255, 0.92);
    color: #13273f;
    border: 1px solid rgba(19, 39, 63, 0.08);
    border-top-left-radius: 6px;
}

.editor-ai-panel__message-meta {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 8px;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.04em;
    text-transform: uppercase;
    opacity: 0.72;
}

.editor-ai-panel__message-text {
    font-size: 13px;
    line-height: 1.7;
    white-space: pre-wrap;
    word-break: break-word;
}

.editor-ai-panel__message-preview {
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px dashed rgba(19, 39, 63, 0.12);
    color: rgba(19, 39, 63, 0.62);
    font-size: 12px;
    line-height: 1.6;
}

.editor-ai-panel__message-actions {
    margin-top: 10px;
}

.editor-ai-panel__message.is-user .editor-ai-panel__message-preview {
    border-top-color: rgba(255, 255, 255, 0.18);
    color: rgba(255, 255, 255, 0.78);
}

.editor-ai-panel__message-applied {
    margin-top: 10px;
    color: #d1603d;
    font-size: 12px;
    font-weight: 700;
}

.editor-ai-panel__message-error {
    margin-top: 10px;
    color: #c2410c;
    font-size: 12px;
    line-height: 1.6;
}

.editor-ai-panel__composer {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.editor-ai-panel__target-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.editor-ai-panel__target-btn {
    min-height: 34px;
    padding: 0 12px;
    border: 1px solid rgba(19, 39, 63, 0.12);
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.8);
    color: rgba(19, 39, 63, 0.72);
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
}

.editor-ai-panel__target-btn.is-active {
    border-color: rgba(209, 96, 61, 0.28);
    background: rgba(209, 96, 61, 0.1);
    color: #d1603d;
}

.editor-ai-panel__composer-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.editor-ai-panel__footer-text {
    margin: 0;
    color: rgba(19, 39, 63, 0.58);
    font-size: 12px;
    line-height: 1.6;
}

@media (max-width: 768px) {
    .editor-ai-panel__messages {
        max-height: 34vh;
    }

    .editor-ai-panel__composer-footer {
        align-items: flex-start;
        flex-direction: column;
    }
}
</style>
