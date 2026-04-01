<template>
    <div class="c-user-page c-user-profile-edit">
        <div class="c-user-page__decoration c-user-page__decoration--orb"></div>

        <div class="c-user-page__container c-user-page__container--narrow c-user-page__panel c-user-page__panel--float-in c-glass-panel c-user-profile-edit__panel">
            <div class="c-page-header c-page-header--split c-user-profile-edit__header">
                <div>
                    <h2 class="c-page-header__title">编辑个人资料</h2>
                    <p class="c-page-header__subtitle c-user-page__header-note">完善您的个人信息，展示独特的自己</p>
                </div>
                <el-button class="c-user-page__back-link" link @click="$router.back()">
                    <el-icon>
                        <ArrowLeft />
                    </el-icon> 返回个人主页
                </el-button>
            </div>

            <div class="c-user-profile-edit__content" v-loading="loading">
                <el-form :model="form" label-position="top">
                    <div class="c-user-profile-edit__section">
                        <div class="c-user-edit-avatar">
                            <div class="c-user-edit-avatar__frame">
                                <el-upload class="c-user-edit-avatar__upload" action="#" :show-file-list="false"
                                    :http-request="handleUpload" :before-upload="beforeAvatarUpload">
                                    <div class="c-user-edit-avatar__overlay">
                                        <el-icon>
                                            <Camera />
                                        </el-icon>
                                        <span class="c-user-edit-avatar__overlay-text">更换头像</span>
                                    </div>
                                    <img v-if="form.photo" :src="form.photo" alt="用户头像预览" class="c-user-edit-avatar__image" />
                                    <div v-else class="c-user-edit-avatar__placeholder">
                                        <el-icon>
                                            <User />
                                        </el-icon>
                                    </div>
                                </el-upload>
                            </div>
                            <div>
                                <h3 class="c-user-edit-avatar__copy-title">头像设置</h3>
                                <p class="c-user-edit-avatar__copy-desc">支持 JPG/PNG 格式，大小不超过 2MB</p>
                            </div>
                        </div>
                    </div>

                    <div class="c-user-profile-edit__form-grid">
                        <el-form-item label="用户名" class="c-form-field">
                            <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
                        </el-form-item>

                        <el-form-item label="个人简介" class="c-form-field">
                            <el-input v-model="form.profile" type="textarea" :rows="4" placeholder="写一句话介绍一下自己.."
                                resize="none" />
                        </el-form-item>
                    </div>

                    <div class="c-form-actions c-user-profile-edit__actions">
                        <el-button class="c-form-submit c-user-profile-edit__action" type="primary" @click="onSubmit" :loading="submitting"
                            size="large">
                            保存修改
                        </el-button>
                        <el-button class="c-form-secondary c-user-profile-edit__action" @click="$router.back()" size="large">取消</el-button>
                    </div>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getUserBySelf, updateUser, uploadAvatar } from "@/views/user/_domain/user.js";
import { ElMessage } from 'element-plus';
import { Camera, User, ArrowLeft } from '@element-plus/icons-vue';
import router from "@/router/index.js";

const loading = ref(false);
const submitting = ref(false);
const form = ref({
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
        const res = await uploadAvatar(options.file);
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
            // 按照 utils/http.js 的拦截器逻辑，如果 code=200，直接返回 res.data
            // FileController 返回 ImageResult.success(new Img(...))
            // ImageResult 应该也是 CommonResult 的一种变体或者类似结构
            // 如果拦截器已经解包了 data，那 res 就是 Img 对象
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
    const isLt10M = rawFile.size / 1024 / 1024 < 10;

    if (!isImage) {
        ElMessage.error('头像必须是 JPG/PNG/GIF 格式!');
        return false;
    }
    if (!isLt10M) {
        ElMessage.error('头像大小不能超过 10MB!');
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
        await updateUser({
            username: form.value.username,
            photo: form.value.photo,
            profile: form.value.profile
        });
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
