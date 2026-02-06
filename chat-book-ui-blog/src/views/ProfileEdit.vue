<template>
    <div class="profile-edit-page">
        <!-- Background decoration -->
        <div class="bg-decoration"></div>

        <div class="edit-container animate-fade-in">
            <div class="page-header">
                <div class="header-content">
                    <h2>编辑个人资料</h2>
                    <p class="subtitle">完善您的个人信息，展示独特的自己</p>
                </div>
                <el-button class="back-btn" link @click="$router.back()">
                    <el-icon>
                        <ArrowLeft />
                    </el-icon> 返回个人主页
                </el-button>
            </div>

            <div class="edit-content" v-loading="loading">
                <el-form :model="form" label-position="top" class="edit-form">
                    <div class="form-section">
                        <div class="avatar-section">
                            <div class="avatar-wrapper">
                                <el-upload class="avatar-uploader" action="#" :show-file-list="false"
                                    :http-request="handleUpload" :before-upload="beforeAvatarUpload">
                                    <div class="avatar-overlay">
                                        <el-icon>
                                            <Camera />
                                        </el-icon>
                                        <span>更换头像</span>
                                    </div>
                                    <img v-if="form.photo" :src="form.photo" class="avatar" />
                                    <div v-else class="avatar-placeholder">
                                        <el-icon>
                                            <User />
                                        </el-icon>
                                    </div>
                                </el-upload>
                            </div>
                            <div class="avatar-info">
                                <h3>头像设置</h3>
                                <p>支持 JPG/PNG 格式，大小不超过 2MB</p>
                            </div>
                        </div>
                    </div>

                    <div class="form-grid">
                        <el-form-item label="用户名" class="custom-field">
                            <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
                        </el-form-item>

                        <el-form-item label="个人简介" class="custom-field">
                            <el-input v-model="form.profile" type="textarea" :rows="4" placeholder="写一句话介绍一下自己..."
                                resize="none" />
                        </el-form-item>
                    </div>

                    <div class="form-actions">
                        <el-button class="submit-btn" type="primary" @click="onSubmit" :loading="submitting"
                            size="large">
                            保存修改
                        </el-button>
                        <el-button class="cancel-btn" @click="$router.back()" size="large">取消</el-button>
                    </div>
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
import { Camera, User, ArrowLeft } from '@element-plus/icons-vue';
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
    height: 100%;
    background-color: var(--bg-color-base);
    position: relative;
    overflow: hidden;
    display: flex;
    justify-content: center;
    align-items: flex-start;
}

/* Background Decoration */
.bg-decoration {
    position: absolute;
    top: -100px;
    right: -100px;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, var(--color-primary-light) 0%, rgba(255, 255, 255, 0) 70%);
    opacity: 0.6;
    z-index: 0;
    pointer-events: none;
}

.edit-container {
    position: relative;
    z-index: 1;
    width: 100%;
    max-width: 720px;
    background: var(--bg-color-glass);
    backdrop-filter: var(--blur-large);
    border: 1px solid rgba(255, 255, 255, 0.6);
    border-radius: var(--border-radius-xl);
    padding: 40px;
    box-shadow: var(--box-shadow-glass);
    margin-top: 20px;
}

/* Animations */
.animate-fade-in {
    animation: fadeIn 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
    opacity: 0;
    transform: translateY(20px);
}

@keyframes fadeIn {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* Header */
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 40px;
}

.header-content h2 {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 700;
    color: var(--text-color-primary);
    letter-spacing: -0.5px;
}

.subtitle {
    margin: 0;
    color: var(--text-color-secondary);
    font-size: 14px;
}

.back-btn {
    font-weight: 500;
    color: var(--text-color-secondary);
    transition: var(--transition-fast);
}

.back-btn:hover {
    color: var(--color-primary);
}

/* Avatar Section */
.form-section {
    margin-bottom: 40px;
}

.avatar-section {
    display: flex;
    align-items: center;
    gap: 24px;
}

.avatar-wrapper {
    position: relative;
    width: 100px;
    height: 100px;
}

.avatar-uploader :deep(.el-upload) {
    border: 2px solid var(--bg-color-white);
    border-radius: 50%;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 100px;
    height: 100px;
    box-shadow: var(--box-shadow-base);
    transition: var(--transition-base);
}

.avatar-uploader :deep(.el-upload:hover) {
    transform: scale(1.05);
    box-shadow: var(--box-shadow-hover);
}

.avatar {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
}

.avatar-placeholder {
    width: 100%;
    height: 100%;
    background: var(--color-primary-light);
    display: flex;
    justify-content: center;
    align-items: center;
    color: var(--color-primary);
    font-size: 32px;
}

.avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: white;
    opacity: 0;
    transition: var(--transition-fast);
    z-index: 2;
}

.avatar-overlay span {
    font-size: 12px;
    margin-top: 4px;
}

.avatar-uploader :deep(.el-upload:hover) .avatar-overlay {
    opacity: 1;
}

.avatar-info h3 {
    margin: 0 0 4px 0;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-color-primary);
}

.avatar-info p {
    margin: 0;
    font-size: 13px;
    color: var(--text-color-secondary);
}

/* Form Styling */
.form-grid {
    display: grid;
    gap: 24px;
    margin-bottom: 40px;
}

.custom-field :deep(.el-form-item__label) {
    font-weight: 600;
    color: var(--text-color-primary);
    padding-bottom: 8px;
}

.custom-field :deep(.el-input__wrapper),
.custom-field :deep(.el-textarea__inner) {
    background-color: rgba(255, 255, 255, 0.5);
    box-shadow: none;
    border: 1px solid var(--border-color-base);
    border-radius: var(--border-radius-base);
    padding: 12px 16px;
    transition: var(--transition-base);
}

.custom-field :deep(.el-input__wrapper:hover),
.custom-field :deep(.el-textarea__inner:hover) {
    background-color: var(--bg-color-white);
    border-color: var(--color-primary);
}

.custom-field :deep(.el-input__wrapper.is-focus),
.custom-field :deep(.el-textarea__inner:focus) {
    background-color: var(--bg-color-white);
    box-shadow: 0 0 0 3px var(--color-primary-light);
    border-color: var(--color-primary);
}

/* Buttons */
.form-actions {
    display: flex;
    gap: 16px;
    margin-top: 40px;
}

.submit-btn {
    padding: 12px 32px;
    font-weight: 600;
    border-radius: var(--border-radius-base);
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
    transition: var(--transition-base);
}

.submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(37, 99, 235, 0.3);
}

.cancel-btn {
    padding: 12px 32px;
    border-radius: var(--border-radius-base);
    background: transparent;
    border: 1px solid var(--border-color-base);
}

.cancel-btn:hover {
    background: var(--bg-color-white);
    color: var(--text-color-primary);
    border-color: var(--text-color-secondary);
}

/* Responsive */
@media (max-width: 768px) {
    .profile-edit-page {
        padding: 20px;
    }

    .edit-container {
        padding: 24px;
    }

    .avatar-section {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
    }

    .form-actions {
        flex-direction: column;
    }

    .submit-btn,
    .cancel-btn {
        width: 100%;
    }
}
</style>
