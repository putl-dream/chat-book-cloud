<template>
    <footer class="site-footer">
        <div class="site-footer__inner">
            <span class="site-footer__brand">ChatBook</span>
            <span class="site-footer__divider"></span>
            <span class="site-footer__meta">内容探索与创作平台</span>
            <span class="site-footer__divider"></span>
            <span class="site-footer__copyright">© {{ currentYear }} ChatBook</span>
        </div>
    </footer>
</template>

<script setup>
const currentYear = new Date().getFullYear();
</script>

<style scoped>
.site-footer {
    width: 100%;
    padding: 10px 24px 18px;
    display: flex;
    justify-content: center;
    position: relative;
    z-index: 5;
    pointer-events: none;
}

.site-footer__inner {
    width: min(100%, 1180px);
    padding-top: 12px;
    border-top: 1px solid rgba(148, 163, 184, 0.14);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    flex-wrap: wrap;
    text-align: center;
    color: var(--text-color-secondary);
    opacity: 0.72;
}

.site-footer__brand,
.site-footer__meta,
.site-footer__copyright {
    font-size: 12px;
    line-height: 1.4;
}

.site-footer__brand {
    font-weight: 600;
    color: var(--text-color-regular);
}

.site-footer__divider {
    width: 4px;
    height: 4px;
    border-radius: 50%;
    background: rgba(148, 163, 184, 0.46);
    flex-shrink: 0;
}

@media (max-width: 768px) {
    .site-footer {
        padding: 8px 12px 14px;
    }

    .site-footer__inner {
        gap: 8px;
        padding-top: 10px;
    }

    .site-footer__brand,
    .site-footer__meta,
    .site-footer__copyright {
        font-size: 11px;
    }
}
</style>
