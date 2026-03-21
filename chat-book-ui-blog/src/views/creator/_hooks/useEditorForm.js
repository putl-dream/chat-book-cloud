import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { uploadFile } from '@/views/article/_domain/article.js';
import { isValidCoverFile } from '../_domain/editor.js';

export function useEditorForm() {
    const publishDialogVisible = ref(false);
    
    const publishForm = ref({
        category: null,
        contentType: 0,
        tagIds: [],
        abstractText: '',
        cover: ''
    });

    const techTags = ref([]);
    const pathTags = ref([]);
    const selectedTechTags = ref([]);
    const selectedPathTag = ref(null);

    const updateTagIds = () => {
        const techIds = selectedTechTags.value || [];
        const pathId = selectedPathTag.value ? [selectedPathTag.value] : [];
        publishForm.value.tagIds = [...techIds, ...pathId];
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
        techTags,
        pathTags,
        selectedTechTags,
        selectedPathTag,
        updateTagIds,
        handleCoverUpload,
        beforeCoverUpload
    };
}
