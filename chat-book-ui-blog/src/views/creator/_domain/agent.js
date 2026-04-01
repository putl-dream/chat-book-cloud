import request from '@/utils/http.js';
import { buildRichTextEditorHtml } from '@/components/common/rich-text/content-pipeline.js';

export const AGENT_SCENE_TYPE = Object.freeze({
    CREATE: 'CREATE',
    OPTIMIZE: 'OPTIMIZE'
});

const EDITOR_IMPORT_KEY = 'chat-book-agent-editor-import';

export function createAgentSession(params) {
    return request.post('/agent/session/create', params);
}

export function getAgentSessionDetail(sessionId) {
    return request.get('/agent/session/detail', {
        params: { sessionId }
    });
}

export function chatWithAgent(params) {
    return request.post('/agent/session/chat', params);
}

export function generateAgentDraft(params) {
    return request.post('/agent/draft/generate', params);
}

export function optimizeAgentDraft(params) {
    return request.post('/agent/draft/optimize', params);
}

export function adoptAgentDraftVersion(params) {
    return request.post('/agent/draft/version/adopt', params);
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
        tagIds: [],
        cover: '',
        savedAt: new Date().toISOString()
    };

    window.sessionStorage.setItem(EDITOR_IMPORT_KEY, JSON.stringify(payload));
    return true;
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
