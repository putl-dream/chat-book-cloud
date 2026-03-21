import StarterKit from '@tiptap/starter-kit';
import Image from '@tiptap/extension-image';
import Placeholder from '@tiptap/extension-placeholder';
import CharacterCount from '@tiptap/extension-character-count';
import TextAlign from '@tiptap/extension-text-align';
import Highlight from '@tiptap/extension-highlight';
import { Color } from '@tiptap/extension-color';
import { TextStyle } from '@tiptap/extension-text-style';
import TaskList from '@tiptap/extension-task-list';
import TaskItem from '@tiptap/extension-task-item';

import SlashCommand from '@/views/creator/components/slash-command/index.js';
import suggestion from '@/views/creator/components/slash-command/suggestion.js';

export function createRichTextExtensions(options = {}) {
    const { placeholder = '请输入内容...', enableSlashCommand = true } = options;

    const extensions = [
        StarterKit,
        Image,
        Highlight.configure({ multicolor: true }),
        TextStyle,
        Color,
        TaskList,
        TaskItem.configure({ nested: true }),
        Placeholder.configure({ placeholder }),
        CharacterCount,
        TextAlign.configure({ types: ['heading', 'paragraph'] })
    ];

    if (enableSlashCommand) {
        extensions.splice(7, 0, SlashCommand.configure({ suggestion }));
    }

    return extensions;
}

export const richTextEditorAttributes = {
    class: 'rich-text-editor__surface'
};
