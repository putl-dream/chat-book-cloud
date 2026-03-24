<template>
    <div
        class="rich-text-editor content-theme"
        :data-content-variant="variant"
        :data-content-style="theme || null"
        :data-readonly="String(!editable)"
        :data-placeholder="placeholder">
        <EditorContent
            v-if="resolvedEditor"
            :editor="resolvedEditor"
            class="rich-text-editor__content rich-text-viewer__body" />
    </div>
</template>

<script setup>
import { computed, unref, watch } from 'vue';
import { EditorContent } from '@tiptap/vue-3';
import '@/styles/themes/article/light.css';
import '@/styles/themes/article/reading.css';

const props = defineProps({
    editor: {
        type: [Object, Function],
        default: null
    },
    editable: {
        type: Boolean,
        default: true
    },
    placeholder: {
        type: String,
        default: '请输入内容...'
    },
    variant: {
        type: String,
        default: 'editor'
    },
    theme: {
        type: String,
        default: ''
    }
});

const resolvedEditor = computed(() => unref(props.editor));

watch(
    [resolvedEditor, () => props.editable],
    ([editor, editable]) => {
        if (editor?.setEditable) {
            editor.setEditable(editable);
        }
    },
    { immediate: true }
);
</script>

<style scoped>
.rich-text-editor,
.rich-text-editor__content {
    width: 100%;
    min-height: 100%;
}

.rich-text-editor {
    display: flex;
    flex: 1;
    min-height: 0;
}

.rich-text-editor__content {
    display: flex;
    flex: 1;
    min-height: 100%;
}
</style>
