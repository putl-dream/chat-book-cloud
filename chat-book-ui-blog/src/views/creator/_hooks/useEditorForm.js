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
        authorTags: [],
        abstractText: '',
        articleType: ARTICLE_TYPE_ENUM.ORIGINAL,
        creationStatements: [],
        cover: ''
    });

    const authorTagOptions = ref([]);

    const setAuthorTags = (tags = []) => {
        const normalized = Array.isArray(tags) ? tags.filter(Boolean) : [];
        publishForm.value.authorTags = normalized;
        const existingNames = (authorTagOptions.value || []).map((item) => item.name);
        authorTagOptions.value = [...new Set([...existingNames, ...normalized])].map((name) => ({ id: name, name }));
    };

    const setAuthorTagOptions = (options = []) => {
        authorTagOptions.value = Array.isArray(options) ? options : [];
    };

    const mergeAuthorTagOptions = (tags = []) => {
        const nextNames = [...new Set([...(authorTagOptions.value || []).map((item) => item.name), ...tags])];
        authorTagOptions.value = nextNames.map((name) => ({ id: name, name }));
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
        authorTagOptions,
        setAuthorTags,
        setAuthorTagOptions,
        mergeAuthorTagOptions,
        handleCoverUpload,
        beforeCoverUpload
    };
}
