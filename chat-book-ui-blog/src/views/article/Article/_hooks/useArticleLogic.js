import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { queryArticle } from "@/api/article.js";
import { addBrowse, updateCollection, updatePraise } from "@/api/interaction.js";
import { checkLogin } from "@/utils/http.js";

export function useArticleLogic(articleIdRef) {
  const article = ref({});
  const praiseStat = ref(0);
  const collectStat = ref(0);
  const activePanel = ref('default');
  /** 右侧面板是否展开（default 时也需为 true 才能显示作者等） */
  const showRightPanel = ref(false);
  const articleLoading = ref(false);

  const rightSidebarWidth = ref(400);
  const isResizing = ref(false);

  const componentMap = {
    'default': null,
    'comment': null,
    'ai': null
  };

  const queryArticleRequest = async () => {
    if (articleLoading.value) return;
    articleLoading.value = true;
    try {
      const currentId = typeof articleIdRef === 'object' ? articleIdRef.value : articleIdRef;
      const res = await queryArticle(currentId);
      if (res) {
        article.value = res;
        praiseStat.value = article.value.praiseStat;
        collectStat.value = article.value.collectStat;
        try {
          await addBrowse(currentId);
          article.value.viewCount = (article.value.viewCount || 0) + 1;
        } catch (error) {
          console.error('Failed to record browse:', error);
        }
      }
    } finally {
      articleLoading.value = false;
    }
  };

  const handleLike = async () => {
    if (!checkLogin()) return;
    const currentId = typeof articleIdRef === 'object' ? articleIdRef.value : articleIdRef;
    const res = await updatePraise(currentId);
    praiseStat.value = res;
  };

  const handleComment = () => {
    if (activePanel.value === 'comment') {
      showRightPanel.value = false;
      activePanel.value = 'default';
    } else {
      showRightPanel.value = true;
      activePanel.value = 'comment';
    }
  };

  const handleAiChat = () => {
    if (activePanel.value === 'ai') {
      showRightPanel.value = false;
      activePanel.value = 'default';
    } else {
      showRightPanel.value = true;
      activePanel.value = 'ai';
    }
  };

  /** 点击作者名：右侧切换为 default 模块（作者卡片等） */
  const openAuthorPanel = () => {
    if (showRightPanel.value && activePanel.value === 'default') {
      showRightPanel.value = false;
      activePanel.value = 'default';
    } else {
      showRightPanel.value = true;
      activePanel.value = 'default';
    }
  };

  const handleFavorite = async () => {
    if (!checkLogin()) return;
    const currentId = typeof articleIdRef === 'object' ? articleIdRef.value : articleIdRef;
    const res = await updateCollection(currentId);
    if (res === 0) {
      ElMessage.warning('取消收藏');
    } else {
      ElMessage.success('收藏成功');
    }
    collectStat.value = res;
  };

  const startResize = () => {
    isResizing.value = true;
    document.addEventListener('mousemove', handleResize);
    document.addEventListener('mouseup', stopResize);
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
  };

  const handleResize = (e) => {
    if (!isResizing.value) return;
    const container = document.querySelector('.article-detail');
    if (container) {
      const containerRect = container.getBoundingClientRect();
      let newWidth = containerRect.right - e.clientX - 24;

      if (newWidth < 280) newWidth = 280;
      if (newWidth > 700) newWidth = 700;

      rightSidebarWidth.value = newWidth;
    }
  };

  const stopResize = () => {
    isResizing.value = false;
    document.removeEventListener('mousemove', handleResize);
    document.removeEventListener('mouseup', stopResize);
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
  };

  return {
    article,
    praiseStat,
    collectStat,
    activePanel,
    showRightPanel,
    rightSidebarWidth,
    isResizing,
    componentMap,
    queryArticleRequest,
    handleLike,
    handleComment,
    handleAiChat,
    handleFavorite,
    openAuthorPanel,
    startResize
  };
}
