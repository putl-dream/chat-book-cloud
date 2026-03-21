import { ref, watch, onBeforeUnmount } from 'vue';

export function useArticleToc(editorRef) {
    const headings = ref([]);
    const activeId = ref('');

    const updateHeadings = () => {
        const editor = editorRef.value;
        if (!editor) return;

        const transaction = editor.state.tr;
        let modified = false;
        const newHeadings = [];

        editor.state.doc.descendants((node, pos) => {
            if (node.type.name === 'heading') {
                const id = node.attrs.id || `heading-${Math.random().toString(36).substring(2, 9)}`;

                if (!node.attrs.id) {
                    transaction.setNodeMarkup(pos, undefined, { ...node.attrs, id });
                    modified = true;
                }

                newHeadings.push({
                    level: node.attrs.level,
                    text: node.textContent,
                    id: id,
                    pos: pos
                });
            }
        });

        if (modified) {
            editor.view.dispatch(transaction);
        }

        headings.value = newHeadings;
    };

    const scrollToHeading = (id) => {
        const editor = editorRef.value;
        if (!editor) return;

        const element = editor.view.dom.querySelector(`[id="${id}"]`);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
            activeId.value = id;
        }
    };

    watch(editorRef, (newEditor) => {
        if (newEditor) {
            newEditor.on('update', updateHeadings);
            updateHeadings();
        }
    }, { immediate: true });

    onBeforeUnmount(() => {
        const editor = editorRef.value;
        if (editor) {
            editor.off('update', updateHeadings);
        }
    });

    return {
        headings,
        activeId,
        scrollToHeading
    };
}
