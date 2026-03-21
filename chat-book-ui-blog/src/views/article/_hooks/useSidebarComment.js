import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { getByArticleId, saveReview, flattenCommentChildren } from "@/views/article/_domain/interaction.js";
import { checkLogin } from "@/utils/http.js";

export function useSidebarComment(articleIdRef) {
    const mainCommentContent = ref('');
    const replyContent = ref('');
    const activeReplyId = ref(null);
    const comments = ref([]);
    const submitting = ref(false);

    const totalComments = computed(() => {
        let count = comments.value.length;
        comments.value.forEach(c => {
            if (c.children) count += c.children.length;
        });
        return count;
    });

    const queryCommentRequest = async () => {
        try {
            const articleId = typeof articleIdRef === 'object' ? articleIdRef.value : articleIdRef;
            if (!articleId) return;
            const res = await getByArticleId(articleId);
            if (res) {
                const oldExpandedState = {};
                comments.value.forEach(c => {
                    if (c.id) oldExpandedState[c.id] = c.expanded;
                });

                comments.value = res.map(comment => ({
                    ...comment,
                    expanded: oldExpandedState[comment.id] !== undefined ? oldExpandedState[comment.id] : true,
                    children: flattenCommentChildren(comment.children, comment.username)
                }));
            }
        } catch (error) {
            console.error("Failed to fetch comments", error);
        }
    };

    const handleKeydown = (e, isMain = false) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            if (isMain) {
                handleMainSubmit();
            } else {
                submitReply();
            }
        }
    };

    const handleMainSubmit = () => {
        submitComment(0, mainCommentContent.value);
    };

    const submitReply = () => {
        const targetId = activeReplyId.value;
        const content = replyContent.value;
        if (targetId) {
            submitComment(targetId, content);
        }
    };

    const submitComment = async (parentId, content) => {
        if (!checkLogin()) return;

        if (!content || content.trim() === '') {
            ElMessage.warning('请输入评论内容');
            return;
        }

        submitting.value = true;
        try {
            const articleId = typeof articleIdRef === 'object' ? articleIdRef.value : articleIdRef;
            await saveReview({
                articleId: articleId,
                parentId: parentId,
                content: content,
            });

            ElMessage.success('发布成功');

            if (parentId === 0) {
                mainCommentContent.value = '';
            } else {
                replyContent.value = '';
                activeReplyId.value = null;
            }

            await queryCommentRequest();
        } catch (error) {
            console.error("Publish comment failed", error);
        } finally {
            submitting.value = false;
        }
    };

    const toggleExpand = (comment) => {
        comment.expanded = !comment.expanded;
    };

    const toggleReply = (id) => {
        if (activeReplyId.value === id) {
            activeReplyId.value = null;
            replyContent.value = '';
        } else {
            activeReplyId.value = id;
            replyContent.value = '';
        }
    };

    return {
        mainCommentContent,
        replyContent,
        activeReplyId,
        comments,
        submitting,
        totalComments,
        queryCommentRequest,
        handleKeydown,
        handleMainSubmit,
        submitReply,
        toggleExpand,
        toggleReply
    };
}
