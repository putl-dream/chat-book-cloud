import StarterKit from '@tiptap/starter-kit';
import Image from '@tiptap/extension-image';
import Placeholder from '@tiptap/extension-placeholder';
import CharacterCount from '@tiptap/extension-character-count';
import TextAlign from '@tiptap/extension-text-align';
import Highlight from '@tiptap/extension-highlight';
import { Color } from '@tiptap/extension-color';
import { TextStyle } from '@tiptap/extension-text-style';
import TaskList from '@tiptap/extension-task-list';

import SlashCommand from '@/views/creator/components/slash-command/index.js';
import suggestion from '@/views/creator/components/slash-command/suggestion.js';
import { MarkdownPaste, MarkdownTaskItem } from '@/components/common/rich-text/markdown-shortcuts.js';

export function createRichTextExtensions(options = {}) {
    const { placeholder = '请输入内容...', enableSlashCommand = true } = options;

    const extensions = [
        StarterKit,
        Image,
        Highlight.configure({ multicolor: true }),
        TextStyle,
        Color,
        TaskList,
        MarkdownTaskItem.configure({ nested: true }),
        MarkdownPaste,
        Placeholder.configure({ placeholder }),
        CharacterCount,
        TextAlign.configure({ types: ['heading', 'paragraph'] })
    ];

    if (enableSlashCommand) {
        extensions.splice(7, 0, SlashCommand.configure({ suggestion }));
    }

    return extensions;
}

export const createRichTextEditorAttributes = ({ spellcheck = false } = {}) => ({
    spellcheck: String(spellcheck),
    autocorrect: spellcheck ? 'on' : 'off',
    autocapitalize: spellcheck ? 'on' : 'off',
    autocomplete: spellcheck ? 'on' : 'off',
    'data-spellcheck': spellcheck ? 'on' : 'off'
});

export const applyRichTextEditorAttributes = (editor, options = {}) => {
    const surface = editor?.view?.dom;

    if (!surface?.setAttribute) {
        return;
    }

    const attributes = createRichTextEditorAttributes(options);

    Object.entries(attributes).forEach(([key, value]) => {
        surface.setAttribute(key, String(value));
    });
};
