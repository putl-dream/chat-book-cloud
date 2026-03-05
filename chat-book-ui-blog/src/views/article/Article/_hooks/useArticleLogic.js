import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { queryArticle } from "@/api/article.js";
import { updateCollection, updatePraise } from "@/api/user.js";
import { checkLogin } from "@/utils/http.js";

export function useArticleLogic(articleId) {
  const article = ref({});
  const praiseStat = ref(0);
  const collectStat = ref(0);
  const activePanel = ref('default');

  const rightSidebarWidth = ref(300);
  const isResizing = ref(false);

  const componentMap = {
    'default': null,
    'comment': null,
    'ai': null
  };

  const queryArticleRequest = async () => {
    const res = await queryArticle(articleId);
    if (res) {
      article.value = res;
      praiseStat.value = article.value.praiseStat;
      collectStat.value = article.value.collectStat;
    }
  };

  const handleLike = async () => {
    if (!checkLogin()) return;
    const res = await updatePraise(articleId);
    praiseStat.value = res;
  };

  const handleComment = () => {
    activePanel.value = activePanel.value === 'comment' ? 'default' : 'comment';
  };

  const handleAiChat = () => {
    activePanel.value = activePanel.value === 'ai' ? 'default' : 'ai';
  };

  const handleFavorite = async () => {
    if (!checkLogin()) return;
    const res = await updateCollection(articleId);
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

      if (newWidth < 200) newWidth = 200;
      if (newWidth > 600) newWidth = 600;

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
    rightSidebarWidth,
    isResizing,
    componentMap,
    queryArticleRequest,
    handleLike,
    handleComment,
    handleAiChat,
    handleFavorite,
    startResize
  };
}
