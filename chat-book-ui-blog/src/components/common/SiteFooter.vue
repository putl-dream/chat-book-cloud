<template>
  <footer class="site-footer">
    <div class="site-footer__inner">
      <div class="site-footer__main">
        <span class="site-footer__brand">ChatBook</span>
        <span class="site-footer__divider" aria-hidden="true"></span>
        <span class="site-footer__meta">内容探索与创作平台</span>
        <span class="site-footer__divider hidden-mobile" aria-hidden="true"></span>
        <span class="site-footer__copyright">
                    &copy; <time :datetime="currentYear">{{ currentYear }}</time> ChatBook
                </span>
      </div>

      <div class="site-footer__compliance" v-if="icpNumber">
        <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer" class="footer-link">
          {{ icpNumber }}
        </a>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref } from 'vue';

const currentYear = new Date().getFullYear().toString();

// 预留备案号变量。等备案审核通过后，直接填入即可。
const icpNumber = ref('京ICP备XXXXXXXX号-1');
</script>

<style scoped>
.site-footer {
  width: 100%;
  padding: 20px 24px;
  display: flex;
  justify-content: center;
  position: relative;
  z-index: 5;
  /* 移除了 pointer-events: none; 以保证 a 标签可以被点击 */
  background: transparent;
}

.site-footer__inner {
  width: min(100%, 1180px);
  padding-top: 16px;
  border-top: 1px solid rgba(148, 163, 184, 0.14);
  display: flex;
  flex-direction: column; /* 改为纵向排列，分层显示版权和备案 */
  align-items: center;
  gap: 8px;
  text-align: center;
  color: var(--text-color-secondary, #64748b);
}

.site-footer__main {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.site-footer__brand,
.site-footer__meta,
.site-footer__copyright {
  font-size: 13px;
  line-height: 1.5;
  letter-spacing: 0.3px;
}

.site-footer__brand {
  font-weight: 600;
  color: var(--text-color-regular, #334155);
}

.site-footer__divider {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(148, 163, 184, 0.46);
  flex-shrink: 0;
}

/* 链接基础样式 */
.footer-link {
  font-size: 12px;
  color: var(--text-color-secondary, #64748b);
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-link:hover {
  color: var(--color-primary, #6366f1);
}

/* 移动端专属响应式优化 */
@media (max-width: 768px) {
  .site-footer {
    padding: 16px 16px 24px;
  }

  .site-footer__inner {
    padding-top: 12px;
    gap: 6px;
  }

  .site-footer__main {
    gap: 6px;
  }

  .site-footer__brand,
  .site-footer__meta,
  .site-footer__copyright {
    font-size: 12px;
  }

  /* 在小屏幕下，隐藏第二个圆点，强制版权信息换行，视觉更整齐 */
  .hidden-mobile {
    display: none;
  }

  .site-footer__copyright {
    width: 100%;
    margin-top: 4px;
  }
}
</style>