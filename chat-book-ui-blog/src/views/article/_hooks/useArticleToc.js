import { ref, watch, onBeforeUnmount, nextTick } from 'vue';
import { createHeadingId } from '@/components/common/rich-text/content-pipeline.js';

export function useArticleToc(editorRef) {
    const headings = ref([]);
    const activeId = ref('');
    
    let observer = null;
    let isClickScrolling = false;
    let clickScrollTimer = null;
    const visibleHeadings = new Set();

    const setupObserver = () => {
        if (observer) observer.disconnect();
        
        const editor = editorRef.value;
        if (!editor || headings.value.length === 0) return;

        let root = null;
        if (editor.view.dom) {
            root = editor.view.dom.closest('.scroll-area') || editor.view.dom.closest('.custom-scrollbar');
        }

        observer = new IntersectionObserver((entries) => {
            // Update the set of visible headings
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    visibleHeadings.add(entry.target.id);
                } else {
                    visibleHeadings.delete(entry.target.id);
                }
            });

            // Prevent observer from hijacking activeId during smooth click-scrolling
            if (isClickScrolling) return;

            let newActiveId = activeId.value;

            if (visibleHeadings.size > 0) {
                // If there are visible headings, the top-most one in DOM order is considered active
                const visibleArray = headings.value.filter(h => visibleHeadings.has(h.id));
                if (visibleArray.length > 0) {
                    newActiveId = visibleArray[0].id;
                }
            } else {
                // Manually find the closest heading bounding box strictly above the viewport
                let foundId = '';
                const threshold = window.innerHeight * 0.3; // Give a 30% tolerance margin from the top
                
                // Scan heading elements from bottom to top
                for (let i = headings.value.length - 1; i >= 0; i--) {
                    const h = headings.value[i];
                    const el = editor.view.dom.querySelector(`[id="${h.id}"]`);
                    if (el) {
                        const rect = el.getBoundingClientRect();
                        if (rect.top <= threshold) {
                            foundId = h.id;
                            break;
                        }
                    }
                }

                if (foundId) {
                    newActiveId = foundId;
                }
            }

            if (newActiveId !== activeId.value) {
                activeId.value = newActiveId;
            }
        }, {
            root,
            rootMargin: '0px 0px 0px 0px', // Standard observe entire viewport
            threshold: 0
        });

        // Delay observing slightly to ensure DOM nodes are successfully completely painted
        setTimeout(() => {
            visibleHeadings.clear();
            headings.value.forEach(heading => {
                const el = editor.view.dom.querySelector(`[id="${heading.id}"]`);
                if (el) observer.observe(el);
            });
            
            // Set initial active state if nothing is intersecting on load
            if (!activeId.value && headings.value.length > 0) {
                activeId.value = headings.value[0].id;
            }
        }, 150);
    };

    const updateHeadings = () => {
        const editor = editorRef.value;
        if (!editor) return;

        const transaction = editor.state.tr;
        let modified = false;
        const newHeadings = [];
        const headingOccurrences = new Map();

        editor.state.doc.descendants((node, pos) => {
            if (node.type.name === 'heading') {
                const id = createHeadingId(node.textContent || '', headingOccurrences, `section-${newHeadings.length + 1}`);

                if (node.attrs.id !== id) {
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

        // Re-setup observer after DOM changes
        nextTick(() => {
            setupObserver();
        });
    };

    const scrollToHeading = (id) => {
        const editor = editorRef.value;
        if (!editor) return;

        const element = editor.view.dom.querySelector(`[id="${id}"]`);
        if (element) {
            isClickScrolling = true;
            activeId.value = id; // Immediately highlight active TOC block
            
            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
            
            // Clear previous timeout and hold the lock for duration of scroll animation
            if (clickScrollTimer) clearTimeout(clickScrollTimer);
            clickScrollTimer = setTimeout(() => {
                isClickScrolling = false;
            }, 800); // 800ms covers standard smooth scroll max duration
        }
    };

    watch(editorRef, (newEditor) => {
        if (newEditor) {
            newEditor.on('update', updateHeadings);
            updateHeadings();
        }
    }, { immediate: true });

    onBeforeUnmount(() => {
        if (observer) observer.disconnect();
        if (clickScrollTimer) clearTimeout(clickScrollTimer);
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
