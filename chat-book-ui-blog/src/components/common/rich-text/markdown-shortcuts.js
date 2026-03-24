import { Extension, wrappingInputRule } from '@tiptap/core';
import TaskItem from '@tiptap/extension-task-item';
import { Plugin, PluginKey } from '@tiptap/pm/state';

import { buildRichTextEditorHtml } from './content-pipeline.js';

export const markdownTaskListInputRegex = /^\s*(?:[-+*]\s)?\[([ xX])\]\s$/;
const markdownLineBreakRegex = /\r\n?/g;
const markdownBlockLineRegex = /^\s*(#{1,6}\s|>\s|[-+*]\s(?:\[[ xX]\]\s)?|\d+\.\s|```|~~~)/;

const normalizeClipboardText = (value = '') => String(value).replace(markdownLineBreakRegex, '\n');

export const shouldHandleMarkdownPaste = ({ text = '', html = '', hasFiles = false } = {}) => {
    if (hasFiles || html.trim()) {
        return false;
    }

    const normalizedText = normalizeClipboardText(text).trim();

    if (!normalizedText) {
        return false;
    }

    return normalizedText
        .split('\n')
        .some((line) => markdownBlockLineRegex.test(line));
};

export const buildMarkdownPasteHtml = (text = '') => {
    const normalizedText = normalizeClipboardText(text);

    if (!shouldHandleMarkdownPaste({ text: normalizedText })) {
        return '';
    }

    return buildRichTextEditorHtml(normalizedText, 'markdown');
};

export const MarkdownTaskItem = TaskItem.extend({
    addInputRules() {
        return [
            wrappingInputRule({
                find: markdownTaskListInputRegex,
                type: this.type,
                getAttributes: (match) => ({
                    checked: String(match[1] || '').toLowerCase() === 'x'
                })
            })
        ];
    }
});

export const MarkdownPaste = Extension.create({
    name: 'markdownPaste',

    addProseMirrorPlugins() {
        return [
            new Plugin({
                key: new PluginKey('markdownPaste'),
                props: {
                    handlePaste: (_view, event) => {
                        if (!event?.clipboardData || this.editor.isActive('codeBlock')) {
                            return false;
                        }

                        const html = event.clipboardData.getData('text/html') || '';
                        const text = event.clipboardData.getData('text/plain') || '';
                        const hasFiles = Boolean(event.clipboardData.files?.length);

                        if (!shouldHandleMarkdownPaste({ text, html, hasFiles })) {
                            return false;
                        }

                        const content = buildMarkdownPasteHtml(text);

                        if (!content) {
                            return false;
                        }

                        event.preventDefault();

                        return this.editor
                            .chain()
                            .focus(undefined, { scrollIntoView: false })
                            .insertContent(content)
                            .run();
                    }
                }
            })
        ];
    }
});
