import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getUserDraftArticlePage, deleteArticle } from '@/views/article/_domain/article.js';

export function useDraftLogic() {
    const router = useRouter();

    const drafts = ref([]);
    const totalDrafts = ref(0);
    const pageSize = ref(10);
    const currentPage = ref(1);

    const fetchDrafts = async () => {
        try {
            const response = await getUserDraftArticlePage(currentPage.value, pageSize.value);
            if (response === null) return;
            drafts.value = response.list;
            totalDrafts.value = parseInt(response.total);
        } catch (error) {
            console.error('Failed to fetch drafts:', error);
        }
    };

    const handlePageChange = (newPage) => {
        currentPage.value = newPage;
        fetchDrafts();
    };

    const handleEdit = (draft) => {
        router.push(`/text/${draft.id}`);
    };

    const handleDelete = async (draft) => {
        try {
            await ElMessageBox.confirm(
                `确定要删除草稿"${draft.title || '无标题'}" 吗？`,
                '删除确认',
                {
                    confirmButtonText: '确定删除',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            );

            await deleteArticle(draft.id);
            ElMessage.success('草稿删除成功');
            fetchDrafts();
        } catch (e) {
            // Cancelled deletion
        }
    };

    onMounted(() => {
        fetchDrafts();
    });

    return {
        drafts,
        totalDrafts,
        pageSize,
        currentPage,
        handlePageChange,
        handleEdit,
        handleDelete
    };
}

export function useDraftFormatter() {
    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        return dateStr.split('T')[0];
    };

    return {
        formatDate
    };
}
