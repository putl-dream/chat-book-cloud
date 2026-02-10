<template>
  <div class="tiptap-toolbar" v-if="editor">
    <div class="toolbar-group">
      <el-tooltip content="加粗 (Ctrl+B)" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('bold') }"
          @click="editor.chain().focus().toggleBold().run()">
          <el-icon>
            <Bold />
          </el-icon>
        </button>
      </el-tooltip>

      <el-tooltip content="斜体 (Ctrl+I)" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('italic') }"
          @click="editor.chain().focus().toggleItalic().run()">
          <el-icon>
            <Italic />
          </el-icon>
        </button>
      </el-tooltip>

      <el-tooltip content="下划线 (Ctrl+U)" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('underline') }"
          @click="editor.chain().focus().toggleUnderline().run()">
          <span style="text-decoration: underline; font-weight: bold;">U</span>
        </button>
      </el-tooltip>

      <el-tooltip content="删除线" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('strike') }"
          @click="editor.chain().focus().toggleStrike().run()">
          <span style="text-decoration: line-through; font-weight: bold;">S</span>
        </button>
      </el-tooltip>
    </div>

    <div class="divider"></div>

    <div class="toolbar-group">
      <el-tooltip content="一级标题" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('heading', { level: 1 }) }"
          @click="editor.chain().focus().toggleHeading({ level: 1 }).run()">
          H1
        </button>
      </el-tooltip>

      <el-tooltip content="二级标题" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('heading', { level: 2 }) }"
          @click="editor.chain().focus().toggleHeading({ level: 2 }).run()">
          H2
        </button>
      </el-tooltip>

      <el-tooltip content="三级标题" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('heading', { level: 3 }) }"
          @click="editor.chain().focus().toggleHeading({ level: 3 }).run()">
          H3
        </button>
      </el-tooltip>
    </div>

    <div class="divider"></div>

    <div class="toolbar-group">
      <el-tooltip content="左对齐" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive({ textAlign: 'left' }) }"
          @click="editor.chain().focus().setTextAlign('left').run()">
          <el-icon><img src="https://api.iconify.design/tabler:align-left.svg" width="16" height="16"
              alt="left" /></el-icon>
          <!-- Fallback using text if icon fails or use simple svg -->
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none"
            stroke-linecap="round" stroke-linejoin="round">
            <line x1="17" y1="10" x2="3" y2="10"></line>
            <line x1="21" y1="6" x2="3" y2="6"></line>
            <line x1="21" y1="14" x2="3" y2="14"></line>
            <line x1="17" y1="18" x2="3" y2="18"></line>
          </svg>
        </button>
      </el-tooltip>

      <el-tooltip content="居中对齐" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive({ textAlign: 'center' }) }"
          @click="editor.chain().focus().setTextAlign('center').run()">
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none"
            stroke-linecap="round" stroke-linejoin="round">
            <line x1="21" y1="6" x2="3" y2="6"></line>
            <line x1="17" y1="10" x2="7" y2="10"></line>
            <line x1="21" y1="14" x2="3" y2="14"></line>
            <line x1="17" y1="18" x2="7" y2="18"></line>
          </svg>
        </button>
      </el-tooltip>

      <el-tooltip content="右对齐" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive({ textAlign: 'right' }) }"
          @click="editor.chain().focus().setTextAlign('right').run()">
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none"
            stroke-linecap="round" stroke-linejoin="round">
            <line x1="21" y1="6" x2="3" y2="6"></line>
            <line x1="21" y1="10" x2="7" y2="10"></line>
            <line x1="21" y1="14" x2="3" y2="14"></line>
            <line x1="21" y1="18" x2="7" y2="18"></line>
          </svg>
        </button>
      </el-tooltip>
    </div>

    <div class="divider"></div>

    <div class="toolbar-group">
      <el-tooltip content="无序列表" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('bulletList') }"
          @click="editor.chain().focus().toggleBulletList().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none"
            stroke-linecap="round" stroke-linejoin="round">
            <line x1="8" y1="6" x2="21" y2="6"></line>
            <line x1="8" y1="12" x2="21" y2="12"></line>
            <line x1="8" y1="18" x2="21" y2="18"></line>
            <line x1="3" y1="6" x2="3.01" y2="6"></line>
            <line x1="3" y1="12" x2="3.01" y2="12"></line>
            <line x1="3" y1="18" x2="3.01" y2="18"></line>
          </svg>
        </button>
      </el-tooltip>

      <el-tooltip content="有序列表" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('orderedList') }"
          @click="editor.chain().focus().toggleOrderedList().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none"
            stroke-linecap="round" stroke-linejoin="round">
            <line x1="10" y1="6" x2="21" y2="6"></line>
            <line x1="10" y1="12" x2="21" y2="12"></line>
            <line x1="10" y1="18" x2="21" y2="18"></line>
            <path d="M4 6h1v4"></path>
            <path d="M4 10h2"></path>
            <path d="M6 18H4c0-1 2-2 2-3s-1-1.5-2-1"></path>
          </svg>
        </button>
      </el-tooltip>
    </div>

    <div class="divider"></div>

    <div class="toolbar-group">
      <el-tooltip content="引用" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('blockquote') }"
          @click="editor.chain().focus().toggleBlockquote().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none"
            stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
          </svg>
        </button>
      </el-tooltip>

      <el-tooltip content="代码块" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('codeBlock') }"
          @click="editor.chain().focus().toggleCodeBlock().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none"
            stroke-linecap="round" stroke-linejoin="round">
            <polyline points="16 18 22 12 16 6"></polyline>
            <polyline points="8 6 2 12 8 18"></polyline>
          </svg>
        </button>
      </el-tooltip>
    </div>

    <div class="divider"></div>

    <div class="toolbar-group">
      <el-tooltip content="插入图片" placement="bottom" :show-after="500">
        <button class="toolbar-btn" @click="triggerImageUpload">
          <el-icon>
            <Picture />
          </el-icon>
        </button>
      </el-tooltip>
      <input type="file" ref="fileInput" style="display: none" accept="image/*" @change="handleImageUpload">
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { API_CONFIG } from "@/config/index.js";

const props = defineProps({
  editor: {
    type: Object,
    required: true,
  },
});

const fileInput = ref(null);

const triggerImageUpload = () => {
  fileInput.value.click();
};

const handleImageUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // Validate file size (e.g., 2MB)
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB');
    return;
  }

  const formData = new FormData();
  formData.append('file', file);

  try {
    ElMessage.info('正在上传图片...');
    // Assuming API structure based on previous Text.vue config
    const token = localStorage.getItem('token');
    const response = await axios.post(`${API_CONFIG.baseURL}/article/file/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'token': token
      }
    });

    if (response.data && response.data.code === 0) { // Assuming 0 is success code
      const url = response.data.data; // Adjust based on actual response structure
      props.editor.chain().focus().setImage({ src: url }).run();
      ElMessage.success('图片上传成功');
    } else {
      // Fallback for different response structure, maybe direct url or code 200
      if (response.data.data) {
        props.editor.chain().focus().setImage({ src: response.data.data }).run();
        ElMessage.success('图片上传成功');
      } else {
        ElMessage.error('图片上传失败: ' + (response.data.msg || '未知错误'));
      }
    }
  } catch (error) {
    console.error('Upload error:', error);
    ElMessage.error('图片上传出错');
  } finally {
    // Reset input
    event.target.value = '';
  }
};
</script>

<style scoped>
.tiptap-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.divider {
  width: 1px;
  height: 20px;
  background-color: rgba(0, 0, 0, 0.1);
  margin: 0 8px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  color: #4b5563;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 600;
}

.toolbar-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: #111827;
}

.toolbar-btn.is-active {
  background-color: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}
</style>
