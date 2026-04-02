import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { uploadFile } from '@/views/article/_domain/article.js';
import { isValidCoverFile } from '../_domain/editor.js';
import { ARTICLE_TYPE_ENUM } from '@/constants';

export function useEditorForm() {
    const publishDialogVisible = ref(false);
    
    const publishForm = ref({
        category: null,
        contentType: 0,
        tagIds: [],
        abstractText: '',
        articleType: ARTICLE_TYPE_ENUM.ORIGINAL,
        creationStatements: [],
        cover: ''
    });

    const topicTags = ref([]);
    const techTags = ref([]);
    const pathTags = ref([]);
    const selectedTopicTags = ref([]);
    const selectedTechTags = ref([]);
    const selectedPathTag = ref(null);

    const updateTagIds = () => {
        const topicIds = selectedTopicTags.value || [];
        const techIds = selectedTechTags.value || [];
        const pathId = selectedPathTag.value ? [selectedPathTag.value] : [];
        publishForm.value.tagIds = [...topicIds, ...techIds, ...pathId];
    };

    const handleCoverUpload = async (option) => {
        try {
            const res = await uploadFile(option.file);
            if (res && res.url) {
                publishForm.value.cover = res.url;
                ElMessage.success('上传成功');
            } else {
                ElMessage.error('上传失败');
            }
        } catch (e) {
            console.error(e);
            ElMessage.error('上传出错');
        }
    };

    const beforeCoverUpload = (rawFile) => {
        if (!isValidCoverFile(rawFile)) {
            ElMessage.error('Avatar picture must be JPG/PNG format and size < 2MB!');
            return false;
        }
        return true;
    };

    return {
        publishDialogVisible,
        publishForm,
        topicTags,
        techTags,
        pathTags,
        selectedTopicTags,
        selectedTechTags,
        selectedPathTag,
        updateTagIds,
        handleCoverUpload,
        beforeCoverUpload
    };
}
