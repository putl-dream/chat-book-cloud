<template>
    <div
        class="rich-text-editor content-theme"
        :data-content-variant="variant"
        :data-readonly="String(!editable)"
        :data-placeholder="placeholder">
        <EditorContent
            v-if="resolvedEditor"
            :editor="resolvedEditor"
            class="rich-text-editor__content" />
    </div>
</template>

<script setup>
import { computed, unref, watch } from 'vue';
import { EditorContent } from '@tiptap/vue-3';

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
