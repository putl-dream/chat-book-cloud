<template>
    <section class="interactive-card">
        <div class="interactive-card__accent"></div>

        <div v-if="isSummaryMode" class="interactive-card__summary">
            <div class="interactive-card__meta">
                <span class="interactive-card__eyebrow">需求卡片</span>
                <span class="interactive-card__progress">已完成</span>
            </div>
            <h4 class="interactive-card__title">{{ form.title || '已收到你的补充信息' }}</h4>
            <p v-if="message.content" class="interactive-card__description">{{ message.content }}</p>

            <div class="interactive-card__summary-list">
                <div
                    v-for="item in summaryItems"
                    :key="item.id"
                    class="interactive-card__summary-item"
                >
                    <span class="interactive-card__summary-label">{{ item.label }}</span>
                    <span class="interactive-card__summary-value">{{ item.value }}</span>
                </div>
            </div>

            <button
                class="interactive-card__ghost-btn"
                type="button"
                :disabled="disabled"
                @click="handleEdit"
            >
                点击修改
            </button>
        </div>

        <div v-else-if="currentQuestion" class="interactive-card__editor">
            <div class="interactive-card__meta">
                <span class="interactive-card__eyebrow">结构化提问</span>
                <span class="interactive-card__progress">{{ currentIndex + 1 }} / {{ totalQuestions }}</span>
            </div>

            <h4 class="interactive-card__title">{{ form.title || '先补充几个关键信息' }}</h4>
            <p v-if="displayDescription" class="interactive-card__description">{{ displayDescription }}</p>

            <div class="interactive-card__progressbar">
                <span class="interactive-card__progressbar-fill" :style="{ width: progressWidth }"></span>
            </div>

            <div class="interactive-card__question">
                <div class="interactive-card__question-head">
                    <span class="interactive-card__question-index">Q{{ currentIndex + 1 }}</span>
                    <span class="interactive-card__question-text">{{ currentQuestion.label }}</span>
                    <span v-if="!currentQuestion.required" class="interactive-card__optional">可跳过</span>
                </div>

                <div v-if="currentQuestion.type === 'single_choice'" class="interactive-card__chips">
                    <button
                        v-for="option in currentQuestion.options"
                        :key="option.value"
                        type="button"
                        class="interactive-card__chip"
                        :class="{ 'is-selected': isSingleSelected(option.value) }"
                        :disabled="disabled"
                        @click="handleSingleSelect(option.value)"
                    >
                        <span>{{ option.label }}</span>
                        <small v-if="option.description">{{ option.description }}</small>
                    </button>
                </div>

                <div v-else-if="currentQuestion.type === 'multi_choice'" class="interactive-card__chips">
                    <button
                        v-for="option in currentQuestion.options"
                        :key="option.value"
                        type="button"
                        class="interactive-card__chip"
                        :class="{ 'is-selected': isMultiSelected(option.value) }"
                        :disabled="disabled"
                        @click="toggleMultiSelect(option.value)"
                    >
                        <span>{{ option.label }}</span>
                        <small v-if="option.description">{{ option.description }}</small>
                    </button>
                </div>

                <el-input
                    v-if="currentQuestion.type === 'text_input'"
                    v-model="textAnswer"
                    type="textarea"
                    resize="none"
                    :autosize="{ minRows: 3, maxRows: 5 }"
                    :placeholder="currentQuestion.placeholder || '请输入你的补充说明'"
                    :disabled="disabled"
                    class="interactive-card__textarea"
                />
            </div>

            <div class="interactive-card__actions">
                <button
                    type="button"
                    class="interactive-card__ghost-btn"
                    :disabled="disabled || currentIndex === 0"
                    @click="handlePrev"
                >
                    上一步
                </button>
                <button
                    v-if="!currentQuestion.required"
                    type="button"
                    class="interactive-card__ghost-btn"
                    :disabled="disabled"
                    @click="handleSkip"
                >
                    跳过
                </button>
                <button
                    type="button"
                    class="interactive-card__primary-btn"
                    :disabled="disabled || !canContinue"
                    @click="handleNext"
                >
                    {{ isLastQuestion ? '提交' : '下一步' }}
                </button>
            </div>
        </div>
    </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import {
    buildAnswerRecord,
    buildInteractionResponsePayload,
    formatAnswerValue,
    hasAnswer,
    normalizeInteractiveFormPayload
} from '@/views/creator/_domain/agent-interaction.js';

const props = defineProps({
    message: {
        type: Object,
        required: true
    },
    disabled: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['submit']);

const editing = ref(false);
const currentIndex = ref(0);
const localAnswers = ref({});

const form = computed(() => normalizeInteractiveFormPayload(props.message?.payload) || {
    formId: '',
    title: '',
    description: '',
    questions: []
});
const response = computed(() => props.message?.interactionResponse || null);
const totalQuestions = computed(() => form.value.questions.length);
const currentQuestion = computed(() => form.value.questions[currentIndex.value] || null);
const isLastQuestion = computed(() => currentIndex.value >= totalQuestions.value - 1);
const isSummaryMode = computed(() => Boolean(response.value) && !editing.value);
const progressWidth = computed(() => {
    if (!totalQuestions.value) {
        return '0%';
    }
    return `${((currentIndex.value + 1) / totalQuestions.value) * 100}%`;
});
const displayDescription = computed(() => {
    if (currentQuestion.value && currentQuestion.value !== form.value.questions[0]) {
        return form.value.description || '';
    }
    return props.message?.content || form.value.description || '';
});
const canContinue = computed(() => {
    if (!currentQuestion.value) {
        return false;
    }
    if (!currentQuestion.value.required) {
        return true;
    }
    return hasAnswer(currentQuestion.value, localAnswers.value);
});
const summaryItems = computed(() => form.value.questions
    .map((question) => {
        const value = buildAnswerRecord(response.value)[question.id];
        const formatted = formatAnswerValue(value);
        if (!formatted) {
            return null;
        }
        return {
            id: question.id,
            label: question.label,
            value: formatted
        };
    })
    .filter(Boolean));
const textAnswer = computed({
    get() {
        return currentQuestion.value ? String(localAnswers.value[currentQuestion.value.id] || '') : '';
    },
    set(value) {
        if (!currentQuestion.value) {
            return;
        }
        localAnswers.value = {
            ...localAnswers.value,
            [currentQuestion.value.id]: String(value || '')
        };
    }
});

const syncFromProps = () => {
    const answerRecord = buildAnswerRecord(response.value);
    localAnswers.value = { ...answerRecord };
    editing.value = !response.value;
    currentIndex.value = resolveStartIndex(form.value.questions, localAnswers.value);
};

watch(() => props.message?.id, syncFromProps, { immediate: true });
watch(response, syncFromProps, { deep: true });

function resolveStartIndex(questions, answers) {
    if (!Array.isArray(questions) || !questions.length) {
        return 0;
    }
    const firstPendingIndex = questions.findIndex((question) => question.required && !hasAnswer(question, answers));
    if (firstPendingIndex >= 0) {
        return firstPendingIndex;
    }
    return 0;
}

function isSingleSelected(value) {
    return currentQuestion.value && localAnswers.value[currentQuestion.value.id] === value;
}

function isMultiSelected(value) {
    if (!currentQuestion.value) {
        return false;
    }
    const currentValues = localAnswers.value[currentQuestion.value.id];
    return Array.isArray(currentValues) && currentValues.includes(value);
}

function handleSingleSelect(value) {
    if (!currentQuestion.value || props.disabled) {
        return;
    }
    localAnswers.value = {
        ...localAnswers.value,
        [currentQuestion.value.id]: value
    };
    if (isLastQuestion.value) {
        submitCurrentAnswers();
        return;
    }
    currentIndex.value += 1;
}

function toggleMultiSelect(value) {
    if (!currentQuestion.value || props.disabled) {
        return;
    }
    const currentValues = Array.isArray(localAnswers.value[currentQuestion.value.id])
        ? [...localAnswers.value[currentQuestion.value.id]]
        : [];
    const nextValues = currentValues.includes(value)
        ? currentValues.filter((item) => item !== value)
        : [...currentValues, value];
    localAnswers.value = {
        ...localAnswers.value,
        [currentQuestion.value.id]: nextValues
    };
}

function handlePrev() {
    if (props.disabled || currentIndex.value <= 0) {
        return;
    }
    currentIndex.value -= 1;
}

function handleSkip() {
    if (!currentQuestion.value || currentQuestion.value.required || props.disabled) {
        return;
    }
    const nextAnswers = { ...localAnswers.value };
    delete nextAnswers[currentQuestion.value.id];
    localAnswers.value = nextAnswers;
    if (isLastQuestion.value) {
        submitCurrentAnswers();
        return;
    }
    currentIndex.value += 1;
}

function handleNext() {
    if (!currentQuestion.value || props.disabled || !canContinue.value) {
        return;
    }
    if (isLastQuestion.value) {
        submitCurrentAnswers();
        return;
    }
    currentIndex.value += 1;
}

function submitCurrentAnswers() {
    const payload = buildInteractionResponsePayload(form.value, localAnswers.value);
    if (!payload?.answers?.length) {
        return;
    }
    editing.value = false;
    emit('submit', { ...localAnswers.value });
}

function handleEdit() {
    if (props.disabled) {
        return;
    }
    editing.value = true;
    currentIndex.value = 0;
    localAnswers.value = {
        ...buildAnswerRecord(response.value)
    };
}
</script>

<style scoped>
.interactive-card {
    position: relative;
    width: 100%;
    padding: 18px 18px 16px;
    border-radius: 20px;
    background:
        radial-gradient(circle at top right, rgba(209, 96, 61, 0.16), transparent 42%),
        linear-gradient(180deg, #fcf7f2 0%, #ffffff 100%);
    border: 1px solid rgba(209, 96, 61, 0.14);
    box-shadow: 0 16px 30px rgba(19, 39, 63, 0.08);
    overflow: hidden;
}

.interactive-card__accent {
    position: absolute;
    left: 16px;
    right: 16px;
    top: 0;
    height: 3px;
    border-radius: 999px;
    background: linear-gradient(90deg, rgba(209, 96, 61, 0.95), rgba(19, 39, 63, 0.6));
}

.interactive-card__summary,
.interactive-card__editor {
    position: relative;
    z-index: 1;
}

.interactive-card__meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
}

.interactive-card__eyebrow,
.interactive-card__progress,
.interactive-card__optional,
.interactive-card__question-index {
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
}

.interactive-card__eyebrow {
    color: rgba(209, 96, 61, 0.9);
}

.interactive-card__progress,
.interactive-card__optional,
.interactive-card__question-index {
    color: rgba(19, 39, 63, 0.45);
}

.interactive-card__title {
    margin: 0;
    font-size: 16px;
    line-height: 1.35;
    color: #13273f;
}

.interactive-card__description {
    margin: 8px 0 0;
    font-size: 13px;
    line-height: 1.6;
    color: rgba(19, 39, 63, 0.64);
}

.interactive-card__progressbar {
    width: 100%;
    height: 6px;
    margin: 16px 0 18px;
    border-radius: 999px;
    background: rgba(19, 39, 63, 0.08);
    overflow: hidden;
}

.interactive-card__progressbar-fill {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(90deg, #d1603d, #13273f);
    transition: width 0.24s ease;
}

.interactive-card__question-head {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
    margin-bottom: 12px;
}

.interactive-card__question-text {
    font-size: 14px;
    font-weight: 600;
    color: #13273f;
}

.interactive-card__chips {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.interactive-card__chip {
    min-height: 38px;
    padding: 10px 14px;
    border-radius: 999px;
    border: 1px solid rgba(19, 39, 63, 0.12);
    background: rgba(255, 255, 255, 0.92);
    color: #13273f;
    cursor: pointer;
    transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    font-weight: 600;
}

.interactive-card__chip small {
    color: rgba(19, 39, 63, 0.52);
    font-size: 11px;
    font-weight: 500;
}

.interactive-card__chip:hover:not(:disabled) {
    transform: translateY(-1px);
    border-color: rgba(209, 96, 61, 0.38);
    box-shadow: 0 8px 16px rgba(209, 96, 61, 0.12);
}

.interactive-card__chip.is-selected {
    background: rgba(209, 96, 61, 0.1);
    border-color: rgba(209, 96, 61, 0.5);
    color: #b94725;
}

.interactive-card__textarea {
    margin-top: 4px;
}

.interactive-card__textarea :deep(.el-textarea__inner) {
    border-radius: 14px;
    border-color: rgba(19, 39, 63, 0.12);
    box-shadow: none;
    min-height: 96px;
}

.interactive-card__actions {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 18px;
}

.interactive-card__ghost-btn,
.interactive-card__primary-btn {
    height: 36px;
    padding: 0 14px;
    border-radius: 999px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
}

.interactive-card__ghost-btn {
    border: 1px solid rgba(19, 39, 63, 0.12);
    background: rgba(255, 255, 255, 0.82);
    color: #13273f;
}

.interactive-card__primary-btn {
    border: none;
    background: linear-gradient(135deg, #d1603d, #c04e2b);
    color: #fff;
    box-shadow: 0 10px 16px rgba(209, 96, 61, 0.18);
}

.interactive-card__ghost-btn:hover:not(:disabled),
.interactive-card__primary-btn:hover:not(:disabled) {
    transform: translateY(-1px);
}

.interactive-card__ghost-btn:disabled,
.interactive-card__primary-btn:disabled,
.interactive-card__chip:disabled {
    cursor: not-allowed;
    opacity: 0.5;
    transform: none;
    box-shadow: none;
}

.interactive-card__summary-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin: 16px 0 18px;
}

.interactive-card__summary-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 12px 14px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.8);
    border: 1px solid rgba(19, 39, 63, 0.06);
}

.interactive-card__summary-label {
    font-size: 12px;
    color: rgba(19, 39, 63, 0.52);
}

.interactive-card__summary-value {
    font-size: 14px;
    font-weight: 600;
    color: #13273f;
    line-height: 1.5;
}
</style>
