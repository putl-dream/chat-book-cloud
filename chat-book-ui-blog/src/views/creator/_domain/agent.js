import request from '@/utils/http.js';
import { buildRichTextEditorHtml } from '@/components/common/rich-text/content-pipeline.js';
import { ARTICLE_TYPE_ENUM, CREATION_STATEMENT_ENUM } from '@/constants';

export const AGENT_SCENE_TYPE = Object.freeze({
    DISCUSS: 'DISCUSS',
    LEARN: 'LEARN',
    DRAFT: 'DRAFT',
    EDIT: 'EDIT',
    CREATE: 'CREATE',
    OPTIMIZE: 'OPTIMIZE'
});

export const AGENT_ASSISTANT_ACTION = Object.freeze({
    ASK: 'ASK',
    TEACH: 'TEACH',
    SUGGEST_DRAFT: 'SUGGEST_DRAFT',
    GENERATE_DRAFT: 'GENERATE_DRAFT',
    EDIT_DRAFT: 'EDIT_DRAFT'
});

export const AGENT_DRAFT_READINESS = Object.freeze({
    NOT_READY: 'NOT_READY',
    PARTIAL: 'PARTIAL',
    READY: 'READY'
});

const EDITOR_IMPORT_KEY = 'chat-book-agent-editor-import';
const EDITOR_GENERATION_KEY = 'chat-book-agent-editor-generation';

export function createAgentSession(params) {
    return request.post('/agent/session/create', params);
}

export function getAgentSessionDetail(sessionId) {
    return request.get('/agent/session/detail', {
        params: { sessionId }
    });
}

export function getAgentSessionPage(pageNo = 1, pageSize = 12, keyword = '') {
    return request.get('/agent/session/page', {
        params: { pageNo, pageSize, keyword }
    });
}

export function chatWithAgent(params) {
    return request.post('/agent/session/chat', params, {
        timeout: 60000
    });
}

export function generateAgentDraft(params) {
    return request.post('/agent/draft/generate', params, {
        timeout: 60000
    });
}

export function optimizeAgentDraft(params) {
    return request.post('/agent/draft/optimize', params, {
        timeout: 60000
    });
}

export function adoptAgentDraftVersion(params) {
    return request.post('/agent/draft/version/adopt', params);
}

export function extractArticleSummary(params) {
    return request.post('/agent/draft/summary', params, {
        timeout: 60000
    });
}

export function assistEditorContent(params) {
    return request.post('/agent/editor/assist', params, {
        timeout: 60000
    });
}

export function normalizeAgentDraft(source = {}) {
    return {
        draftId: source.draftId ?? null,
        versionNo: source.versionNo ?? source.currentVersionNo ?? source.candidateVersionNo ?? null,
        title: source.title ?? '',
        summary: source.summary ?? '',
        content: source.content ?? ''
    };
}

export function saveAgentDraftImport(draft) {
    if (typeof window === 'undefined' || !draft) {
        return false;
    }

    const normalized = normalizeAgentDraft(draft);
    const payload = {
        title: normalized.title,
        content: buildRichTextEditorHtml(normalized.content, 'markdown'),
        abstractText: normalized.summary,
        category: null,
        contentType: 0,
        authorTags: [],
        articleType: ARTICLE_TYPE_ENUM.ORIGINAL,
        creationStatements: [CREATION_STATEMENT_ENUM.AI_ASSISTED],
        cover: '',
        savedAt: new Date().toISOString()
    };

    window.sessionStorage.setItem(EDITOR_IMPORT_KEY, JSON.stringify(payload));
    return true;
}

export function saveAgentGenerationIntent(intent) {
    if (typeof window === 'undefined' || !intent?.sessionId) {
        return false;
    }

    const payload = {
        sessionId: Number(intent.sessionId),
        source: intent.source || 'agent-studio',
        createdAt: intent.createdAt || new Date().toISOString()
    };

    window.sessionStorage.setItem(EDITOR_GENERATION_KEY, JSON.stringify(payload));
    return true;
}

export function loadAgentGenerationIntent() {
    if (typeof window === 'undefined') {
        return null;
    }

    const raw = window.sessionStorage.getItem(EDITOR_GENERATION_KEY);
    if (!raw) {
        return null;
    }

    try {
        const payload = JSON.parse(raw);
        if (!payload?.sessionId) {
            clearAgentGenerationIntent();
            return null;
        }
        return payload;
    } catch (error) {
        console.error('Failed to parse agent generation payload:', error);
        clearAgentGenerationIntent();
        return null;
    }
}

export function loadAgentDraftImport() {
    if (typeof window === 'undefined') {
        return null;
    }

    const raw = window.sessionStorage.getItem(EDITOR_IMPORT_KEY);
    if (!raw) {
        return null;
    }

    try {
        return JSON.parse(raw);
    } catch (error) {
        console.error('Failed to parse agent editor import payload:', error);
        clearAgentDraftImport();
        return null;
    }
}

export function clearAgentDraftImport() {
    if (typeof window === 'undefined') {
        return;
    }
    window.sessionStorage.removeItem(EDITOR_IMPORT_KEY);
}

export function clearAgentGenerationIntent() {
    if (typeof window === 'undefined') {
        return;
    }
    window.sessionStorage.removeItem(EDITOR_GENERATION_KEY);
}

function decodeJsonStringFragment(value = '') {
    let candidate = value;
    while (candidate.length > 0) {
        try {
            return JSON.parse(`"${candidate}"`);
        } catch (error) {
            candidate = candidate.slice(0, -1);
        }
    }
    return '';
}

function extractDraftField(buffer = '', fieldName) {
    const matcher = new RegExp(`"${fieldName}"\\s*:\\s*"`, 'm');
    const match = matcher.exec(buffer);
    if (!match) {
        return '';
    }

    let rawValue = '';
    let consecutiveBackslashes = 0;
    for (let cursor = match.index + match[0].length; cursor < buffer.length; cursor += 1) {
        const char = buffer[cursor];
        if (char === '"' && consecutiveBackslashes % 2 === 0) {
            return decodeJsonStringFragment(rawValue);
        }
        rawValue += char;
        consecutiveBackslashes = char === '\\' ? consecutiveBackslashes + 1 : 0;
    }
    return decodeJsonStringFragment(rawValue);
}

export function buildStreamingDraftPreview(buffer = '') {
    const title = extractDraftField(buffer, 'title');
    const summary = extractDraftField(buffer, 'summary');
    const content = extractDraftField(buffer, 'content');
    if (!title && !summary && !content) {
        return null;
    }
    return normalizeAgentDraft({ title, summary, content });
}

export function normalizeSceneType(sceneType, fallback = AGENT_SCENE_TYPE.DISCUSS) {
    if (!sceneType) {
        return fallback;
    }
    const normalized = String(sceneType).toUpperCase();
    if (normalized === AGENT_SCENE_TYPE.CREATE) {
        return AGENT_SCENE_TYPE.DISCUSS;
    }
    if (normalized === AGENT_SCENE_TYPE.OPTIMIZE) {
        return AGENT_SCENE_TYPE.EDIT;
    }
    return Object.values(AGENT_SCENE_TYPE).includes(normalized) ? normalized : fallback;
}

export function normalizeDraftReadiness(value, fallback = AGENT_DRAFT_READINESS.NOT_READY) {
    if (!value) {
        return fallback;
    }
    const normalized = String(value).toUpperCase();
    return Object.values(AGENT_DRAFT_READINESS).includes(normalized) ? normalized : fallback;
}

export function normalizeAssistantAction(value, fallback = AGENT_ASSISTANT_ACTION.ASK) {
    if (!value) {
        return fallback;
    }
    const normalized = String(value).toUpperCase();
    return Object.values(AGENT_ASSISTANT_ACTION).includes(normalized) ? normalized : fallback;
}
