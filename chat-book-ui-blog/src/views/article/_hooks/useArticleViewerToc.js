import { nextTick, onBeforeUnmount, ref, watch } from 'vue';

export function useArticleViewerToc(articleHtmlRef, contentTargetRef) {
    const headings = ref([]);
    const activeId = ref('');
    
    let observer = null;
    let isClickScrolling = false;
    let clickScrollTimer = null;
    const visibleHeadings = new Set();

    const readHeadingsFromHtml = () => {
        if (!articleHtmlRef.value || typeof DOMParser === 'undefined') {
            headings.value = [];
            activeId.value = '';
            return;
        }

        const doc = new DOMParser().parseFromString(`<div data-reader-toc-root>${articleHtmlRef.value}</div>`, 'text/html');
        const root = doc.body.querySelector('[data-reader-toc-root]');

        if (!root) {
            headings.value = [];
            activeId.value = '';
            return;
        }

        headings.value = Array.from(root.querySelectorAll('h1, h2, h3, h4, h5, h6')).map((heading, index) => ({
            id: heading.id || `section-${index + 1}`,
            text: heading.textContent?.trim() || `章节 ${index + 1}`,
            level: Number(heading.tagName.slice(1)) || 1
        }));
    };

    const setupObserver = () => {
        if (observer) observer.disconnect();
        
        const contentTarget = contentTargetRef.value;
        if (!contentTarget || headings.value.length === 0) return;

        observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    visibleHeadings.add(entry.target.id);
                } else {
                    visibleHeadings.delete(entry.target.id);
                }
            });

            if (isClickScrolling) return;

            let newActiveId = activeId.value;

            if (visibleHeadings.size > 0) {
                const visibleArray = headings.value.filter(h => visibleHeadings.has(h.id));
                if (visibleArray.length > 0) {
                    newActiveId = visibleArray[0].id;
                }
            } else {
                // If nothing is visible (e.g., deep inside a long section), manually locate the deepest heading ABOVE the current Viewport.
                let foundId = '';
                const threshold = window.innerHeight * 0.3; // Give a 30% tolerance margin from the top
                
                // Scan heading elements from bottom to top
                for (let i = headings.value.length - 1; i >= 0; i--) {
                    const h = headings.value[i];
                    const el = contentTarget.querySelector(`[id="${h.id}"]`);
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
            root: null, // Use viewport as we're likely using global scroll
            rootMargin: '0px 0px 0px 0px',
            threshold: 0
        });

        setTimeout(() => {
            visibleHeadings.clear();
            headings.value.forEach(heading => {
                const el = contentTarget.querySelector(`[id="${heading.id}"]`);
                if (el) observer.observe(el);
            });
            
            if (!activeId.value && headings.value.length > 0) {
                activeId.value = headings.value[0].id;
            }
        }, 150);
    };

    const scrollToHeading = (id) => {
        const contentTarget = contentTargetRef.value;
        if (!contentTarget) return;

        const element = contentTarget.querySelector(`[id="${id}"]`);
        if (element) {
            isClickScrolling = true;
            activeId.value = id;
            
            // Adjust offset for global fixed headers
            const headerOffset = 80; 
            const elementPosition = element.getBoundingClientRect().top;
            const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
            
            window.scrollTo({
                 top: offsetPosition,
                 behavior: "smooth"
            });
            
            if (clickScrollTimer) clearTimeout(clickScrollTimer);
            clickScrollTimer = setTimeout(() => {
                isClickScrolling = false;
            }, 800);
        }
    };

    watch([articleHtmlRef, contentTargetRef], async ([newHtml, newTarget]) => {
        if (newHtml && newTarget) {
            readHeadingsFromHtml();
            await nextTick();
            setupObserver();
        }
    }, { immediate: true });

    onBeforeUnmount(() => {
        if (observer) observer.disconnect();
        if (clickScrollTimer) clearTimeout(clickScrollTimer);
    });

    return {
        headings,
        activeId,
        scrollToHeading
    };
}
