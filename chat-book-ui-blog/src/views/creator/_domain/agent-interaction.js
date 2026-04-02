export const AGENT_MESSAGE_TYPE = Object.freeze({
    TEXT: 'text',
    INTERACTIVE_FORM: 'interactive_form'
});

const QUESTION_TYPE = Object.freeze({
    SINGLE: 'single_choice',
    MULTI: 'multi_choice',
    TEXT: 'text_input'
});

export function normalizeAgentMessageType(type) {
    const normalized = String(type || '').trim().toLowerCase();
    if (normalized === AGENT_MESSAGE_TYPE.INTERACTIVE_FORM) {
        return AGENT_MESSAGE_TYPE.INTERACTIVE_FORM;
    }
    return AGENT_MESSAGE_TYPE.TEXT;
}

export function parseAgentPayload(payload) {
    if (!payload) {
        return null;
    }
    if (typeof payload === 'string') {
        try {
            return JSON.parse(payload);
        } catch (error) {
            return null;
        }
    }
    return payload;
}

export function normalizeInteractiveFormPayload(payload) {
    const raw = parseAgentPayload(payload);
    if (!raw || typeof raw !== 'object') {
        return null;
    }

    const questions = Array.isArray(raw.questions)
        ? raw.questions
            .map((question, index) => normalizeQuestion(question, index))
            .filter(Boolean)
        : [];

    if (!questions.length) {
        return null;
    }

    return {
        formId: String(raw.formId || `form_${Date.now()}`).trim(),
        title: String(raw.title || '').trim(),
        description: String(raw.description || '').trim(),
        submitMode: 'batch',
        questions
    };
}

function normalizeQuestion(question, index) {
    if (!question || typeof question !== 'object') {
        return null;
    }

    const label = String(question.label || '').trim();
    if (!label) {
        return null;
    }

    const type = normalizeQuestionType(question.type);
    const options = type === QUESTION_TYPE.TEXT
        ? []
        : normalizeOptions(question.options);

    if ((type === QUESTION_TYPE.SINGLE || type === QUESTION_TYPE.MULTI) && !options.length) {
        return null;
    }

    return {
        id: String(question.id || `question_${index + 1}`).trim(),
        label,
        type,
        required: question.required !== false,
        allowCustomInput: Boolean(question.allowCustomInput),
        placeholder: String(question.placeholder || '').trim(),
        options
    };
}

function normalizeQuestionType(type) {
    const normalized = String(type || '').trim().toLowerCase();
    if (normalized === QUESTION_TYPE.MULTI) {
        return QUESTION_TYPE.MULTI;
    }
    if (normalized === QUESTION_TYPE.TEXT) {
        return QUESTION_TYPE.TEXT;
    }
    return QUESTION_TYPE.SINGLE;
}

function normalizeOptions(options) {
    if (!Array.isArray(options)) {
        return [];
    }
    return options
        .map((option) => {
            const label = String(option?.label || option?.value || '').trim();
            const value = String(option?.value || option?.label || '').trim();
            if (!label || !value) {
                return null;
            }
            return {
                label,
                value,
                description: String(option?.description || '').trim()
            };
        })
        .filter(Boolean);
}

export function normalizeMessagePayload(messageType, payload) {
    const raw = parseAgentPayload(payload);
    if (normalizeAgentMessageType(messageType) === AGENT_MESSAGE_TYPE.INTERACTIVE_FORM) {
        return normalizeInteractiveFormPayload(raw);
    }
    return raw;
}

export function extractInteractionResponse(message) {
    return message?.payload?.interactionResponse || null;
}

export function isInteractionResponseMessage(message) {
    return Boolean(extractInteractionResponse(message));
}

export function buildInteractionResponsePayload(form, answersMap = {}) {
    const safeForm = normalizeInteractiveFormPayload(form);
    if (!safeForm) {
        return null;
    }

    const answers = safeForm.questions
        .map((question) => {
            const rawValue = answersMap[question.id];
            const normalizedValue = normalizeAnswerValue(question.type, rawValue);
            if (!hasAnswerValue(question.type, normalizedValue)) {
                return null;
            }
            return {
                questionId: question.id,
                questionLabel: question.label,
                questionType: question.type,
                value: normalizedValue
            };
        })
        .filter(Boolean);

    return {
        formId: safeForm.formId,
        title: safeForm.title,
        description: safeForm.description,
        answers
    };
}

export function buildInteractionResponseSummary(form, answersMap = {}) {
    const payload = buildInteractionResponsePayload(form, answersMap);
    if (!payload?.answers?.length) {
        return '已收到需求补充';
    }
    const summary = payload.answers
        .map((answer) => `${answer.questionLabel}：${formatAnswerValue(answer.value)}`)
        .join('；');
    return `已收到需求：${summary}`;
}

export function buildAnswerRecord(interactionResponse) {
    if (!interactionResponse?.answers?.length) {
        return {};
    }

    return interactionResponse.answers.reduce((result, answer) => {
        if (!answer?.questionId) {
            return result;
        }
        result[answer.questionId] = normalizeAnswerValue(answer.questionType, answer.value);
        return result;
    }, {});
}

export function hasAnswer(question, answersMap = {}) {
    return hasAnswerValue(question?.type, normalizeAnswerValue(question?.type, answersMap[question?.id]));
}

export function formatAnswerValue(value) {
    if (Array.isArray(value)) {
        return value.filter(Boolean).join('、');
    }
    if (value && typeof value === 'object') {
        return JSON.stringify(value);
    }
    return String(value || '').trim();
}

function normalizeAnswerValue(questionType, value) {
    if (questionType === QUESTION_TYPE.MULTI) {
        return Array.isArray(value)
            ? value.map((item) => String(item || '').trim()).filter(Boolean)
            : [];
    }
    if (value == null) {
        return '';
    }
    return String(value).trim();
}

function hasAnswerValue(questionType, value) {
    if (questionType === QUESTION_TYPE.MULTI) {
        return Array.isArray(value) && value.length > 0;
    }
    return String(value || '').trim().length > 0;
}
