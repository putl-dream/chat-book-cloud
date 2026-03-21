import { reactive, ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { EDITOR_CONFIG } from '../_utils/constants.js';

export function useEditorLayout() {
    const layoutState = reactive({
        leftOpen: true,
        rightOpen: false,
        leftWidth: 20,
        rightWidth: 20,
        isMobile: false
    });
    
    const dragging = ref(null);

    const contentWidth = computed(() => {
        let width = 100;
        if (layoutState.leftOpen) width -= layoutState.leftWidth;
        if (layoutState.rightOpen) width -= layoutState.rightWidth;
        return Math.max(width, 0);
    });

    const enforceConstraints = () => {
        if (layoutState.leftOpen && layoutState.rightOpen && layoutState.leftWidth + layoutState.rightWidth > EDITOR_CONFIG.LAYOUT.MAX_TOTAL_SIDE_WIDTH) {
            layoutState.rightWidth = EDITOR_CONFIG.LAYOUT.MAX_TOTAL_SIDE_WIDTH - layoutState.leftWidth;
            if (layoutState.rightWidth < EDITOR_CONFIG.LAYOUT.MIN_PANEL_WIDTH) {
                layoutState.rightWidth = EDITOR_CONFIG.LAYOUT.MIN_PANEL_WIDTH;
                layoutState.leftWidth = EDITOR_CONFIG.LAYOUT.MAX_TOTAL_SIDE_WIDTH - EDITOR_CONFIG.LAYOUT.MIN_PANEL_WIDTH;
            }
        }
    };

    const toggleLeft = () => {
        layoutState.leftOpen = !layoutState.leftOpen;
        enforceConstraints();
    };

    const toggleRight = () => {
        if (layoutState.isMobile) return;
        layoutState.rightOpen = !layoutState.rightOpen;
        enforceConstraints();
    };

    const startDrag = (side) => {
        dragging.value = side;
        document.body.style.cursor = 'col-resize';
    };

    const onMouseMove = (e, containerRef) => {
        if (!dragging.value || !containerRef) return;
        const containerRect = containerRef.getBoundingClientRect();
        const containerWidth = containerRect.width;
        const mouseX = e.clientX - containerRect.left;
        let percentage = (mouseX / containerWidth) * 100;

        if (dragging.value === 'left') {
            const maxLeft = 100 - (layoutState.rightOpen ? layoutState.rightWidth : 0) - 30;
            if (percentage < EDITOR_CONFIG.LAYOUT.MIN_PANEL_WIDTH) percentage = EDITOR_CONFIG.LAYOUT.MIN_PANEL_WIDTH;
            if (percentage > maxLeft) percentage = maxLeft;
            layoutState.leftWidth = percentage;
        } else if (dragging.value === 'right') {
            const maxRight = 100 - (layoutState.leftOpen ? layoutState.leftWidth : 0) - 30;
            let rightPercent = 100 - percentage;
            if (rightPercent < EDITOR_CONFIG.LAYOUT.MIN_PANEL_WIDTH) rightPercent = EDITOR_CONFIG.LAYOUT.MIN_PANEL_WIDTH;
            if (rightPercent > maxRight) rightPercent = maxRight;
            layoutState.rightWidth = rightPercent;
        }
    };

    const onMouseUp = () => {
        if (dragging.value) {
            dragging.value = null;
            document.body.style.cursor = '';
        }
    };

    const checkMobile = () => {
        const isMobile = window.innerWidth <= EDITOR_CONFIG.LAYOUT.MOBILE_BREAKPOINT;
        layoutState.isMobile = isMobile;
        if (isMobile) layoutState.rightOpen = false;
    };

    onMounted(() => {
        checkMobile();
        window.addEventListener('resize', checkMobile);
    });

    onBeforeUnmount(() => {
        window.removeEventListener('resize', checkMobile);
    });

    return {
        layoutState,
        dragging,
        contentWidth,
        toggleLeft,
        toggleRight,
        startDrag,
        onMouseMove,
        onMouseUp
    };
}
