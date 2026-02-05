<template>
    <div class="profile-edit-page">
        <div class="edit-container">
            <div class="page-header">
                <h2>编辑个人资料</h2>
                <el-button link @click="$router.back()">返回个人主页</el-button>
            </div>

            <div class="edit-content" v-loading="loading">
                <el-form :model="form" label-width="80px" class="edit-form">
                    <el-form-item label="头像">
                        <div class="avatar-edit">
                            {{ form.photo }}
                            <el-upload class="avatar-uploader" action="#" :show-file-list="false"
                                :http-request="handleUpload" :before-upload="beforeAvatarUpload">
                                <img v-if="form.photo" :src="form.photo" class="avatar" />
                                <el-icon v-else class="avatar-uploader-icon">
                                    <Plus />
                                </el-icon>
                            </el-upload>
                            <div class="avatar-tip">点击头像可进行更换，支持 JPG/PNG 格式，大小不超过 2MB</div>
                        </div>
                    </el-form-item>

                    <el-form-item label="用户名">
                        <el-input v-model="form.username" placeholder="请输入用户名" />
                    </el-form-item>

                    <el-form-item label="个人简介">
                        <el-input v-model="form.profile" type="textarea" :rows="4" placeholder="请输入个人简介" />
                    </el-form-item>

                    <el-form-item>
                        <el-button type="primary" @click="onSubmit" :loading="submitting">保存修改</el-button>
                        <el-button @click="$router.back()">取消</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getUserBySelf, updateUser } from "@/api/user.js";
import { uploadFile } from "@/api/article.js";
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import router from "@/router/index.js";

const loading = ref(false);
const submitting = ref(false);
const form = ref({
    id: null,
    userId: null,
    username: '',
    photo: '',
    profile: ''
});

const fetchUserData = async () => {
    loading.value = true;
    try {
        const res = await getUserBySelf();
        if (res) {
            form.value = {
                id: res.id,
                userId: res.userId,
                username: res.username,
                photo: res.photo,
                profile: res.profile || res.introduction || ''
            };
        }
    } catch (error) {
        console.error('获取用户信息失败', error);
        ElMessage.error('获取用户信息失败');
    } finally {
        loading.value = false;
    }
};

const handleUpload = async (options) => {
    try {
        const res = await uploadFile(options.file);
        // 根据后端 ImageResult 结构，成功返回 ImageResult<Img>，其中 data 是 Img 对象
        // Img 对象包含 url 字段
        if (res && res.url) {
            form.value.photo = res.url;
            ElMessage.success('头像上传成功');
        } else if (res && res.data && res.data.url) {
            // 兼容一下可能的数据结构差异
            form.value.photo = res.data.url;
            ElMessage.success('头像上传成功');
        } else {
            // 按照 utils/index.js 的拦截器逻辑，如果 code=200，直接返回 res.data
            // FileController 返回 ImageResult.success(new Img(...))
            // ImageResult 应该也是 CommonResult 的一种变体或者类似结构
            // 如果拦截器已经解包了 data，那么 res 就是 Img 对象
            form.value.photo = res.url;
            ElMessage.success('头像上传成功');
        }
    } catch (error) {
        console.error('上传失败', error);
        ElMessage.error('头像上传失败');
    }
};

const beforeAvatarUpload = (rawFile) => {
    const isImage = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png' || rawFile.type === 'image/gif';
    const isLt2M = rawFile.size / 1024 / 1024 < 2;

    if (!isImage) {
        ElMessage.error('头像必须是 JPG/PNG/GIF 格式!');
        return false;
    }
    if (!isLt2M) {
        ElMessage.error('头像大小不能超过 2MB!');
        return false;
    }
    return true;
};

const onSubmit = async () => {
    if (!form.value.username) {
        ElMessage.warning('用户名不能为空');
        return;
    }

    submitting.value = true;
    try {
        await updateUser(form.value);
        ElMessage.success('保存成功');
        setTimeout(() => {
            router.push('/profile');
        }, 1000);
    } catch (error) {
        console.error('保存失败', error);
        ElMessage.error('保存失败');
    } finally {
        submitting.value = false;
    }
};

onMounted(() => {
    fetchUserData();
});
</script>

<style scoped>
.profile-edit-page {
    min-height: 100vh;
    background-color: var(--bg-color-base);
    padding: 40px 20px;
}

.edit-container {
    max-width: 800px;
    margin: 0 auto;
    background: var(--bg-color-white);
    border-radius: var(--border-radius-large);
    padding: 30px;
    box-shadow: var(--box-shadow-base);
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
    border-bottom: 1px solid var(--border-color-lighter);
    padding-bottom: 15px;
}

.page-header h2 {
    margin: 0;
    color: var(--text-color-primary);
}

.edit-content {
    padding: 0 20px;
}

.avatar-edit {
    display: flex;
    align-items: center;
    gap: 20px;
}

.avatar-tip {
    font-size: 14px;
    color: var(--text-color-secondary);
}

.avatar-uploader .avatar {
    width: 100px;
    height: 100px;
    display: block;
    border-radius: 50%;
    object-fit: cover;
}

.avatar-uploader :deep(.el-upload) {
    border: 1px dashed var(--border-color-base);
    border-radius: 50%;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    width: 100px;
    height: 100px;
}

.avatar-uploader :deep(.el-upload:hover) {
    border-color: var(--color-primary);
}

.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    text-align: center;
    line-height: 100px;
}

/* Responsive */
@media (max-width: 768px) {
    .edit-container {
        padding: 20px;
    }

    .avatar-edit {
        flex-direction: column;
        align-items: flex-start;
    }
}
</style>
