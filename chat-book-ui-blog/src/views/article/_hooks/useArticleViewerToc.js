import { nextTick, onBeforeUnmount, ref, watch } from 'vue';

const HEADING_SELECTOR = 'h1, h2, h3, h4, h5, h6';
const ACTIVE_OFFSET = 96;

export function useArticleViewerToc(articleHtmlRef, contentTargetRef) {
    const headings = ref([]);
    const activeId = ref('');

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

        headings.value = Array.from(root.querySelectorAll(HEADING_SELECTOR)).map((heading, index) => ({
            id: heading.id || `section-${index + 1}`,
            text: heading.textContent?.trim() || `章节 ${index + 1}`,
            level: Number(heading.tagName.slice(1)) || 1
        }));

        activeId.value = headings.value[0]?.id || '';
    };

    const updateActiveHeading = () => {
        const contentTarget = contentTargetRef.value;

        if (!contentTarget || headings.value.length === 0) {
            return;
        }

        const contentRect = contentTarget.getBoundingClientRect();
        let currentHeadingId = headings.value[0]?.id || '';

        headings.value.forEach((heading) => {
            const element = contentTarget.querySelector(`[id="${heading.id}"]`);
            if (!element) {
                return;
            }

            const rect = element.getBoundingClientRect();
            if (rect.top - contentRect.top <= ACTIVE_OFFSET) {
                currentHeadingId = heading.id;
            }
        });

        activeId.value = currentHeadingId;
    };

    const scrollToHeading = (id) => {
        const contentTarget = contentTargetRef.value;
        if (!contentTarget) {
            return;
        }

        const element = contentTarget.querySelector(`[id="${id}"]`);
        if (!element) {
            return;
        }

        activeId.value = id;
        element.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    };

    watch(articleHtmlRef, async () => {
        readHeadingsFromHtml();
        await nextTick();
        updateActiveHeading();
    }, { immediate: true });

    watch(contentTargetRef, async (newTarget, oldTarget) => {
        if (oldTarget) {
            oldTarget.removeEventListener('scroll', updateActiveHeading);
        }

        if (!newTarget) {
            return;
        }

        newTarget.addEventListener('scroll', updateActiveHeading, { passive: true });
        await nextTick();
        updateActiveHeading();
    }, { immediate: true });

    onBeforeUnmount(() => {
        if (contentTargetRef.value) {
            contentTargetRef.value.removeEventListener('scroll', updateActiveHeading);
        }
    });

    return {
        headings,
        activeId,
        scrollToHeading
    };
}
