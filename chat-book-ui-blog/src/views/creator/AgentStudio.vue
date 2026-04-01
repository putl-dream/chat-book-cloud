<template>
    <div class="agent-studio c-creator-page c-creator-page--rounded u-animate-fade-in">
        <section class="agent-studio__hero">
            <div class="agent-studio__hero-copy">
                <span class="agent-studio__eyebrow">Agent Studio</span>
                <h1 class="agent-studio__title">把灵感对话，整理成可继续写下去的文章草稿</h1>
                <p class="agent-studio__subtitle">
                    先聊主题、受众和结构，再一键生成首稿。后续优化会产生候选版本，你可以并排比较后采用，再导入正式编辑器继续润色和发布。
                </p>
            </div>

            <div class="agent-studio__hero-meta">
                <div class="agent-studio__title-field">
                    <label for="agent-session-title">会话标题</label>
                    <input
                        id="agent-session-title"
                        v-model="sessionTitle"
                        type="text"
                        :disabled="Boolean(sessionId)"
                        maxlength="40"
                        placeholder="例如：写一篇关于 Spring Cloud 网关设计的实践总结" />
                </div>

                <div class="agent-studio__tags">
                    <span class="agent-studio__tag agent-studio__tag--status">{{ sessionStatusLabel }}</span>
                    <span v-if="sessionId" class="agent-studio__tag">会话 #{{ sessionId }}</span>
                    <span v-if="activeDraftVersion" class="agent-studio__tag">当前版本 V{{ activeDraftVersion }}</span>
                    <span v-if="pendingDraftVersion" class="agent-studio__tag agent-studio__tag--pending">
                        候选版本 V{{ pendingDraftVersion }}
                    </span>
                </div>

                <div class="agent-studio__hero-actions">
                    <el-button
                        class="agent-studio__ghost-btn"
                        :disabled="creatingSession || chatting"
                        @click="openFreshSession">
                        新建会话
                    </el-button>
                    <el-button
                        class="agent-studio__primary-btn"
                        type="primary"
                        :loading="generatingDraft"
                        :disabled="!hasMessages || chatting"
                        @click="createDraftFromSession">
                        生成首稿
                    </el-button>
                </div>
            </div>
        </section>

        <div class="agent-studio__layout">
            <section class="agent-panel agent-panel--chat">
                <header class="agent-panel__header">
                    <div>
                        <p class="agent-panel__kicker">对话区</p>
                        <h2>先把问题聊清楚</h2>
                    </div>
                    <span class="agent-panel__meta">{{ hasMessages ? `${messages.length} 条消息` : '尚未开始' }}</span>
                </header>

                <div class="agent-panel__body agent-panel__body--chat custom-scrollbar">
                    <div v-if="loadingSession" class="agent-panel__placeholder">
                        <el-skeleton :rows="6" animated />
                    </div>

                    <div v-else-if="!hasMessages" class="agent-panel__placeholder">
                        <p class="agent-panel__placeholder-title">从一段高质量提问开始</p>
                        <p class="agent-panel__placeholder-text">
                            试试说明文章主题、目标读者、想解决的问题，以及你已经确定的观点或素材。
                        </p>
                    </div>

                    <div v-else class="agent-chat-list">
                        <article
                            v-for="message in messages"
                            :key="message.id"
                            class="agent-chat-item"
                            :class="`agent-chat-item--${message.role}`">
                            <div class="agent-chat-item__badge">
                                {{ message.role === 'user' ? '你' : message.role === 'assistant' ? 'AI' : '系统' }}
                            </div>
                            <div class="agent-chat-item__card">
                                <p v-if="message.streaming && !message.content" class="agent-chat-item__streaming">
                                    正在思考...
                                </p>
                                <RichTextViewer
                                    v-else
                                    :html="renderMessageHtml(message.content)"
                                    variant="chat" />
                            </div>
                        </article>
                    </div>
                </div>

                <footer class="agent-panel__footer">
                    <el-input
                        v-model="chatInput"
                        type="textarea"
                        resize="none"
                        :rows="5"
                        maxlength="1200"
                        placeholder="例如：帮我写一篇面向 Java 开发者的文章，主题是如何把 Spring Cloud Gateway 做成一层可演进的 API 门户。先跟我确认结构和适用读者。"
                        @keydown.ctrl.enter.prevent="sendMessage" />

                    <div class="agent-panel__footer-bar">
                        <p class="agent-panel__hint">`Ctrl + Enter` 发送消息</p>
                        <el-button
                            class="agent-studio__primary-btn"
                            type="primary"
                            :loading="chatting || creatingSession"
                            @click="sendMessage">
                            发送并继续对话
                        </el-button>
                    </div>
                </footer>
            </section>

            <section class="agent-panel agent-panel--draft">
                <header class="agent-panel__header">
                    <div>
                        <p class="agent-panel__kicker">草稿区</p>
                        <h2>生成、优化、比较</h2>
                    </div>
                    <div class="agent-panel__actions">
                        <el-button
                            class="agent-studio__ghost-btn"
                            :disabled="!hasDraft"
                            @click="importDraftToEditor">
                            导入编辑器
                        </el-button>
                    </div>
                </header>

                <div class="agent-panel__body custom-scrollbar">
                    <div v-if="!hasDraft" class="agent-panel__placeholder">
                        <p class="agent-panel__placeholder-title">首稿还没生成</p>
                        <p class="agent-panel__placeholder-text">
                            当对话里已经明确文章方向后，点击右上角“生成首稿”，这里会出现可预览的初稿和后续候选版本。
                        </p>
                    </div>

                    <div v-else class="agent-draft-stack">
                        <section class="agent-draft-card">
                            <div class="agent-draft-card__header">
                                <div>
                                    <p class="agent-draft-card__eyebrow">当前采用版本</p>
                                    <h3>{{ draft.title || '未命名草稿' }}</h3>
                                </div>
                                <span class="agent-studio__tag">V{{ activeDraftVersion }}</span>
                            </div>

                            <p class="agent-draft-card__summary">{{ draft.summary || '暂无摘要' }}</p>

                            <RichTextViewer
                                class="agent-draft-card__viewer"
                                :html="renderDraftHtml(draft.content)"
                                variant="article" />
                        </section>

                        <section class="agent-optimize-box">
                            <div class="agent-optimize-box__head">
                                <div>
                                    <p class="agent-panel__kicker">优化指令</p>
                                    <h3>告诉 Agent 要怎么改</h3>
                                </div>
                                <el-button
                                    class="agent-studio__primary-btn"
                                    type="primary"
                                    :loading="optimizingDraft"
                                    @click="optimizeCurrentDraft">
                                    生成候选版本
                                </el-button>
                            </div>

                            <el-input
                                v-model="optimizeInstruction"
                                type="textarea"
                                resize="none"
                                :rows="4"
                                maxlength="600"
                                placeholder="例如：把第二部分压缩成一个 checklist，并把结论改得更适合团队分享场景。" />
                        </section>

                        <section v-if="hasCandidateDraft" class="agent-compare">
                            <div class="agent-compare__head">
                                <div>
                                    <p class="agent-panel__kicker">版本对比</p>
                                    <h3>确认候选版本是否值得采用</h3>
                                </div>

                                <div class="agent-compare__actions">
                                    <span
                                        v-for="chip in draftCompareChips"
                                        :key="chip.key"
                                        class="agent-compare__chip"
                                        :class="{ 'is-changed': chip.changed }">
                                        {{ chip.label }}{{ chip.changed ? '已变化' : '未变化' }}
                                    </span>
                                    <el-button
                                        class="agent-studio__primary-btn"
                                        type="primary"
                                        :loading="adoptingCandidate"
                                        @click="adoptCandidateVersion">
                                        采用候选版本
                                    </el-button>
                                </div>
                            </div>

                            <div class="agent-compare__grid">
                                <article class="agent-compare-card">
                                    <div class="agent-compare-card__top">
                                        <p class="agent-compare-card__label">当前版本</p>
                                        <span class="agent-studio__tag">V{{ activeDraftVersion }}</span>
                                    </div>
                                    <h4>{{ draft.title || '未命名草稿' }}</h4>
                                    <p class="agent-compare-card__summary">{{ draft.summary || '暂无摘要' }}</p>
                                    <RichTextViewer
                                        class="agent-compare-card__viewer"
                                        :html="renderDraftHtml(draft.content)"
                                        variant="article" />
                                </article>

                                <article class="agent-compare-card agent-compare-card--candidate">
                                    <div class="agent-compare-card__top">
                                        <p class="agent-compare-card__label">候选版本</p>
                                        <span class="agent-studio__tag agent-studio__tag--pending">
                                            V{{ pendingDraftVersion }}
                                        </span>
                                    </div>
                                    <h4>{{ candidateDraft.title || '未命名候选稿' }}</h4>
                                    <p class="agent-compare-card__summary">
                                        {{ candidateDraft.summary || '暂无摘要' }}
                                    </p>
                                    <RichTextViewer
                                        class="agent-compare-card__viewer"
                                        :html="renderDraftHtml(candidateDraft.content)"
                                        variant="article" />
                                </article>
                            </div>
                        </section>
                    </div>
                </div>
            </section>
        </div>
    </div>
</template>

<script setup>
import { buildRichTextHtml } from '@/components/common/rich-text/content-pipeline.js';
import RichTextViewer from '@/components/common/rich-text/RichTextViewer.vue';
import { useAgentStudioLogic } from './_hooks/useAgentStudioLogic.js';

const {
    loadingSession,
    creatingSession,
    chatting,
    generatingDraft,
    optimizingDraft,
    adoptingCandidate,
    sessionId,
    sessionTitle,
    chatInput,
    optimizeInstruction,
    messages,
    draft,
    candidateDraft,
    hasMessages,
    hasDraft,
    hasCandidateDraft,
    activeDraftVersion,
    pendingDraftVersion,
    draftCompareChips,
    sessionStatusLabel,
    openFreshSession,
    sendMessage,
    createDraftFromSession,
    optimizeCurrentDraft,
    adoptCandidateVersion,
    importDraftToEditor
} = useAgentStudioLogic();

const renderMessageHtml = (content) => buildRichTextHtml(content || '', 'markdown');
const renderDraftHtml = (content) => buildRichTextHtml(content || '', 'markdown');
</script>

<style scoped>
.agent-studio {
    --agent-ink: #16324f;
    --agent-accent: #d1603d;
    --agent-accent-soft: rgba(209, 96, 61, 0.12);
    --agent-surface: rgba(255, 252, 246, 0.84);
    --agent-line: rgba(22, 50, 79, 0.12);
    --agent-shadow: 0 24px 48px rgba(21, 37, 64, 0.08);
    gap: 24px;
    background:
        radial-gradient(circle at top left, rgba(209, 96, 61, 0.12), transparent 28%),
        radial-gradient(circle at right 20%, rgba(22, 50, 79, 0.08), transparent 26%),
        linear-gradient(180deg, rgba(247, 243, 235, 0.96), rgba(241, 245, 249, 0.96));
}

.agent-studio__hero {
    display: grid;
    grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.9fr);
    gap: 24px;
    padding: 30px 32px;
    border: 1px solid rgba(255, 255, 255, 0.7);
    border-radius: 28px;
    background:
        linear-gradient(135deg, rgba(255, 255, 255, 0.82), rgba(255, 250, 243, 0.88)),
        rgba(255, 255, 255, 0.72);
    box-shadow: var(--agent-shadow);
}

.agent-studio__eyebrow {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    border-radius: 999px;
    background: rgba(22, 50, 79, 0.08);
    color: var(--agent-ink);
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.12em;
    text-transform: uppercase;
}

.agent-studio__title {
    margin: 18px 0 14px;
    color: #13273f;
    font-size: clamp(30px, 4vw, 42px);
    line-height: 1.08;
    letter-spacing: -0.04em;
}

.agent-studio__subtitle {
    margin: 0;
    max-width: 60ch;
    color: rgba(19, 39, 63, 0.72);
    font-size: 15px;
    line-height: 1.8;
}

.agent-studio__hero-meta {
    display: flex;
    flex-direction: column;
    gap: 18px;
    padding: 22px;
    border-radius: 24px;
    background:
        linear-gradient(180deg, rgba(247, 242, 233, 0.94), rgba(255, 255, 255, 0.86));
    border: 1px solid rgba(209, 96, 61, 0.12);
}

.agent-studio__title-field {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.agent-studio__title-field label {
    color: var(--agent-ink);
    font-size: 13px;
    font-weight: 700;
}

.agent-studio__title-field input {
    width: 100%;
    height: 48px;
    padding: 0 16px;
    border: 1px solid rgba(22, 50, 79, 0.12);
    border-radius: 16px;
    background: rgba(255, 255, 255, 0.9);
    color: #13273f;
    font-size: 15px;
    outline: none;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.agent-studio__title-field input:focus {
    border-color: rgba(209, 96, 61, 0.5);
    box-shadow: 0 0 0 4px rgba(209, 96, 61, 0.08);
}

.agent-studio__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.agent-studio__tag {
    display: inline-flex;
    align-items: center;
    min-height: 32px;
    padding: 0 12px;
    border-radius: 999px;
    background: rgba(22, 50, 79, 0.08);
    color: var(--agent-ink);
    font-size: 12px;
    font-weight: 600;
}

.agent-studio__tag--status {
    background: rgba(22, 50, 79, 0.14);
}

.agent-studio__tag--pending {
    background: rgba(209, 96, 61, 0.12);
    color: #8c3f26;
}

.agent-studio__hero-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: auto;
}

.agent-studio__primary-btn,
.agent-studio__ghost-btn {
    min-width: 132px;
    border-radius: 999px;
    font-weight: 600;
}

.agent-studio__primary-btn {
    border: none;
    background: linear-gradient(135deg, #d1603d, #c04e2b);
    box-shadow: 0 14px 28px rgba(209, 96, 61, 0.22);
}

.agent-studio__primary-btn:hover,
.agent-studio__primary-btn:focus-visible {
    background: linear-gradient(135deg, #d66a49, #b94725);
}

.agent-studio__ghost-btn {
    border-color: rgba(22, 50, 79, 0.16);
    background: rgba(255, 255, 255, 0.72);
    color: var(--agent-ink);
}

.agent-studio__layout {
    display: grid;
    grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.05fr);
    gap: 24px;
    min-height: 0;
}

.agent-panel {
    min-height: 720px;
    display: flex;
    flex-direction: column;
    border: 1px solid rgba(255, 255, 255, 0.72);
    border-radius: 28px;
    background: var(--agent-surface);
    box-shadow: var(--agent-shadow);
    overflow: hidden;
}

.agent-panel__header,
.agent-panel__footer {
    flex-shrink: 0;
    padding: 24px 24px 20px;
}

.agent-panel__header {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    border-bottom: 1px solid var(--agent-line);
}

.agent-panel__header h2,
.agent-panel__header h3,
.agent-draft-card__header h3,
.agent-optimize-box__head h3,
.agent-compare__head h3 {
    margin: 6px 0 0;
    color: #13273f;
    font-size: 22px;
    letter-spacing: -0.03em;
}

.agent-panel__kicker,
.agent-draft-card__eyebrow,
.agent-compare-card__label {
    margin: 0;
    color: rgba(19, 39, 63, 0.56);
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.12em;
    text-transform: uppercase;
}

.agent-panel__meta {
    align-self: flex-start;
    padding: 8px 12px;
    border-radius: 999px;
    background: rgba(22, 50, 79, 0.06);
    color: rgba(19, 39, 63, 0.74);
    font-size: 12px;
    font-weight: 600;
}

.agent-panel__actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

.agent-panel__body {
    flex: 1;
    min-height: 0;
    padding: 22px 24px 28px;
    overflow-y: auto;
}

.agent-panel__body--chat {
    padding-bottom: 10px;
}

.agent-panel__placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 100%;
    padding: 28px;
    border: 1px dashed rgba(22, 50, 79, 0.18);
    border-radius: 22px;
    background: rgba(255, 255, 255, 0.68);
    text-align: center;
}

.agent-panel__placeholder-title {
    margin: 0 0 10px;
    color: #13273f;
    font-size: 18px;
    font-weight: 700;
}

.agent-panel__placeholder-text {
    margin: 0;
    max-width: 36ch;
    color: rgba(19, 39, 63, 0.7);
    font-size: 14px;
    line-height: 1.8;
}

.agent-chat-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.agent-chat-item {
    display: grid;
    grid-template-columns: 48px minmax(0, 1fr);
    gap: 14px;
    align-items: start;
}

.agent-chat-item__badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    border-radius: 18px;
    background: rgba(22, 50, 79, 0.1);
    color: var(--agent-ink);
    font-size: 13px;
    font-weight: 700;
}

.agent-chat-item--assistant .agent-chat-item__badge {
    background: rgba(209, 96, 61, 0.16);
    color: #8c3f26;
}

.agent-chat-item__card {
    padding: 18px 18px 2px;
    border: 1px solid rgba(22, 50, 79, 0.08);
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.82);
}

.agent-chat-item--assistant .agent-chat-item__card {
    background: linear-gradient(180deg, rgba(255, 249, 244, 0.98), rgba(255, 255, 255, 0.88));
}

.agent-chat-item__streaming {
    margin: 0;
    color: rgba(19, 39, 63, 0.62);
    font-size: 14px;
    line-height: 1.8;
}

.agent-panel__footer {
    border-top: 1px solid var(--agent-line);
    background: rgba(255, 255, 255, 0.82);
}

.agent-panel__footer :deep(.el-textarea__inner),
.agent-optimize-box :deep(.el-textarea__inner) {
    border-radius: 18px;
    border-color: rgba(22, 50, 79, 0.12);
    box-shadow: none;
}

.agent-panel__footer-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    margin-top: 16px;
}

.agent-panel__hint {
    margin: 0;
    color: rgba(19, 39, 63, 0.56);
    font-size: 12px;
}

.agent-draft-stack {
    display: flex;
    flex-direction: column;
    gap: 18px;
}

.agent-draft-card,
.agent-optimize-box,
.agent-compare {
    padding: 22px;
    border: 1px solid rgba(22, 50, 79, 0.1);
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.78);
}

.agent-draft-card__header,
.agent-optimize-box__head,
.agent-compare__head,
.agent-compare-card__top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 14px;
}

.agent-draft-card__summary,
.agent-compare-card__summary {
    margin: 14px 0 0;
    color: rgba(19, 39, 63, 0.72);
    font-size: 14px;
    line-height: 1.8;
}

.agent-draft-card__viewer,
.agent-compare-card__viewer {
    margin-top: 18px;
    max-height: 360px;
    overflow: auto;
    padding: 18px;
    border-radius: 18px;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(248, 250, 252, 0.92));
    border: 1px solid rgba(22, 50, 79, 0.06);
}

.agent-compare__actions {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 10px;
}

.agent-compare__chip {
    display: inline-flex;
    align-items: center;
    min-height: 32px;
    padding: 0 12px;
    border-radius: 999px;
    background: rgba(22, 50, 79, 0.06);
    color: rgba(19, 39, 63, 0.64);
    font-size: 12px;
    font-weight: 600;
}

.agent-compare__chip.is-changed {
    background: var(--agent-accent-soft);
    color: #8c3f26;
}

.agent-compare__grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
    margin-top: 18px;
}

.agent-compare-card {
    padding: 18px;
    border-radius: 22px;
    border: 1px solid rgba(22, 50, 79, 0.1);
    background: linear-gradient(180deg, rgba(245, 247, 250, 0.94), rgba(255, 255, 255, 0.92));
}

.agent-compare-card--candidate {
    border-color: rgba(209, 96, 61, 0.18);
    background: linear-gradient(180deg, rgba(255, 248, 242, 0.96), rgba(255, 255, 255, 0.92));
}

.agent-compare-card h4 {
    margin: 14px 0 0;
    color: #13273f;
    font-size: 18px;
    letter-spacing: -0.02em;
}

@media (max-width: 1280px) {
    .agent-studio__layout {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 900px) {
    .agent-studio__hero {
        grid-template-columns: 1fr;
        padding: 24px;
    }

    .agent-panel {
        min-height: 640px;
    }

    .agent-compare__head,
    .agent-optimize-box__head,
    .agent-panel__header,
    .agent-panel__footer-bar {
        flex-direction: column;
        align-items: stretch;
    }

    .agent-compare__actions {
        justify-content: flex-start;
    }

    .agent-compare__grid {
        grid-template-columns: 1fr;
    }
}
</style>
