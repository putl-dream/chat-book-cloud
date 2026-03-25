import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { queryArticle } from "@/views/article/_domain/article.js";
import { addBrowse, updateCollection, updatePraise } from "@/views/article/_domain/interaction.js";
import { checkLogin } from "@/utils/http.js";
import { getUserBySelf } from "@/views/user/_domain/user.js";
import { followUser, unfollowUser, getFriendRelation } from "@/views/user/_domain/social.js";
import { PANEL_TYPE, SIDEBAR_CONFIG } from "../_utils/config.js";

export function useArticleLogic(articleIdRef) {
  const article = ref({});
  const praiseStat = ref(0);
  const collectStat = ref(0);
  const activePanel = ref(PANEL_TYPE.DEFAULT);
  /** 默认保持右侧阅读辅助面板可见 */
  const showRightPanel = ref(true);
  const articleLoading = ref(false);
  const currentUser = ref(null);
  const authorRelation = ref(-1);
  const isSelfAuthor = ref(false);
  const authorActionLoading = ref(false);

  const rightSidebarWidth = ref(SIDEBAR_CONFIG.DEFAULT_WIDTH);
  const isResizing = ref(false);

  const loadAuthorContext = async (authorId) => {
    authorRelation.value = -1;
    isSelfAuthor.value = false;

    if (!authorId || !localStorage.getItem('token')) {
      currentUser.value = null;
      return;
    }

    try {
      const self = await getUserBySelf();
      currentUser.value = self || null;

      const currentUserId = Number(self?.id ?? self?.userId);
      const normalizedAuthorId = Number(authorId);

      if (!currentUserId || !normalizedAuthorId) {
        return;
      }

      if (currentUserId === normalizedAuthorId) {
        isSelfAuthor.value = true;
        return;
      }

      const relation = await getFriendRelation(normalizedAuthorId);
      authorRelation.value = Number.isFinite(Number(relation)) ? Number(relation) : -1;
    } catch (error) {
      console.error('Failed to load author relation:', error);
    }
  };

  const queryArticleRequest = async () => {
    if (articleLoading.value) return;
    articleLoading.value = true;
    try {
      const currentId = typeof articleIdRef === 'object' ? articleIdRef.value : articleIdRef;
      if (!currentId) return;
      const res = await queryArticle(currentId);
      if (res) {
        article.value = res;
        praiseStat.value = article.value.praiseStat;
        collectStat.value = article.value.collectStat;
        await loadAuthorContext(article.value.userId);
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
    if (activePanel.value === PANEL_TYPE.COMMENT && showRightPanel.value) {
      showRightPanel.value = false;
    } else {
      showRightPanel.value = true;
      activePanel.value = PANEL_TYPE.COMMENT;
    }
  };

  const handleAiChat = () => {
    if (activePanel.value === PANEL_TYPE.AI && showRightPanel.value) {
      showRightPanel.value = false;
    } else {
      showRightPanel.value = true;
      activePanel.value = PANEL_TYPE.AI;
    }
  };

  const openDefaultPanel = () => {
    if (activePanel.value === PANEL_TYPE.DEFAULT && showRightPanel.value) {
      showRightPanel.value = false;
    } else {
      showRightPanel.value = true;
      activePanel.value = PANEL_TYPE.DEFAULT;
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

  const handleFollow = async () => {
    if (!checkLogin()) return;

    const authorId = Number(article.value?.userId);
    if (!authorId || isSelfAuthor.value || authorActionLoading.value) {
      return;
    }

    authorActionLoading.value = true;

    try {
      const message = authorRelation.value >= 0
        ? await unfollowUser(authorId)
        : await followUser(authorId);

      if (message) {
        ElMessage.success(message);
      }

      await loadAuthorContext(authorId);
    } finally {
      authorActionLoading.value = false;
    }
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

      if (newWidth < SIDEBAR_CONFIG.MIN_WIDTH) newWidth = SIDEBAR_CONFIG.MIN_WIDTH;
      if (newWidth > SIDEBAR_CONFIG.MAX_WIDTH) newWidth = SIDEBAR_CONFIG.MAX_WIDTH;

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
    currentUser,
    authorRelation,
    isSelfAuthor,
    authorActionLoading,
    rightSidebarWidth,
    isResizing,
    queryArticleRequest,
    handleLike,
    handleComment,
    handleAiChat,
    handleFavorite,
    handleFollow,
    openDefaultPanel,
    startResize
  };
}
