import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getUserArticlePage, deleteArticle } from '@/views/article/_domain/article.js';

export function useCreativeLogic() {
    const router = useRouter();

    const articles = ref([]);
    const totalArticles = ref(0);
    const pageSize = ref(10);
    const currentPage = ref(1);

    const fetchArticles = async () => {
        try {
            const response = await getUserArticlePage(currentPage.value, pageSize.value);
            if (response === null) return;
            articles.value = response.list;
            totalArticles.value = parseInt(response.total);
        } catch (error) {
            console.error('Failed to fetch articles:', error);
        }
    };

    const handlePageChange = (newPage) => {
        currentPage.value = newPage;
        fetchArticles();
    };

    const handleOpenAgent = () => {
        router.push('/creative/agent');
    };

    const handleEdit = (article) => {
        router.push(`/text/${article.id}`);
    };

    const handleDelete = async (article) => {
        try {
            await ElMessageBox.confirm(
                `确定要删除文章“${article.title}”吗？`,
                '删除确认',
                {
                    confirmButtonText: '确定删除',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            );

            await deleteArticle(article.id);
            ElMessage.success('文章删除成功');
            fetchArticles();
        } catch (e) {
            // Cancelled deletion
        }
    };

    onMounted(() => {
        fetchArticles();
    });

    return {
        articles,
        totalArticles,
        pageSize,
        currentPage,
        handlePageChange,
        handleOpenAgent,
        handleEdit,
        handleDelete
    };
}

export function useCreativeFormatter() {
    const formatNumber = (num) => {
        if (!num) return 0;
        return num > 9999 ? (num / 10000).toFixed(1) + 'w' : num;
    };

    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        return dateStr.split('T')[0];
    };

    return {
        formatNumber,
        formatDate
    };
}
