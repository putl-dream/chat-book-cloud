<template>
  <div class="tiptap-toolbar" v-if="editor">
    <!-- Group 0: TOC Toggle -->
    <div class="toolbar-group">
      <el-tooltip :content="tocVisible ? '隐藏大纲' : '显示大纲'" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': tocVisible }" @click="$emit('toggle-toc')">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.toc" /></svg>
        </button>
      </el-tooltip>
    </div>

    <div class="divider"></div>

    <!-- Group 1: History -->
    <div class="toolbar-group">
      <el-tooltip content="撤销 (Ctrl+Z)" placement="bottom" :show-after="500">
        <button class="toolbar-btn" @click="editor.chain().focus().undo().run()" :disabled="!editor.can().undo()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.undo" /></svg>
        </button>
      </el-tooltip>
      <el-tooltip content="重做 (Ctrl+Shift+Z)" placement="bottom" :show-after="500">
        <button class="toolbar-btn" @click="editor.chain().focus().redo().run()" :disabled="!editor.can().redo()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.redo" /></svg>
        </button>
      </el-tooltip>
      <el-tooltip content="清除格式" placement="bottom" :show-after="500">
        <button class="toolbar-btn" @click="editor.chain().focus().unsetAllMarks().clearNodes().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.clear" /></svg>
        </button>
      </el-tooltip>
    </div>

    <div class="divider"></div>

    <!-- Group 2: Headings & Font -->
    <div class="toolbar-group">
      <el-dropdown trigger="click" @command="handleHeading">
        <button class="toolbar-btn dropdown-btn" :class="{ 'is-active': editor.isActive('heading') }">
          <span class="btn-text">{{ currentHeadingLabel }}</span>
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="p" :class="{ 'is-active': editor.isActive('paragraph') }">正文 (Paragraph)</el-dropdown-item>
            <el-dropdown-item command="1" :class="{ 'is-active': editor.isActive('heading', { level: 1 }) }"><h1>标题 1</h1></el-dropdown-item>
            <el-dropdown-item command="2" :class="{ 'is-active': editor.isActive('heading', { level: 2 }) }"><h2>标题 2</h2></el-dropdown-item>
            <el-dropdown-item command="3" :class="{ 'is-active': editor.isActive('heading', { level: 3 }) }"><h3>标题 3</h3></el-dropdown-item>
            <el-dropdown-item command="4" :class="{ 'is-active': editor.isActive('heading', { level: 4 }) }"><h4>标题 4</h4></el-dropdown-item>
            <el-dropdown-item command="5" :class="{ 'is-active': editor.isActive('heading', { level: 5 }) }"><h5>标题 5</h5></el-dropdown-item>
            <el-dropdown-item command="6" :class="{ 'is-active': editor.isActive('heading', { level: 6 }) }"><h6>标题 6</h6></el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="divider"></div>

    <!-- Group 3: Text Style -->
    <div class="toolbar-group">
      <el-tooltip content="加粗 (Ctrl+B)" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('bold') }" @click="editor.chain().focus().toggleBold().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.bold" /></svg>
        </button>
      </el-tooltip>
      <el-tooltip content="斜体 (Ctrl+I)" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('italic') }" @click="editor.chain().focus().toggleItalic().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.italic" /></svg>
        </button>
      </el-tooltip>
      <el-tooltip content="下划线(Ctrl+U)" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('underline') }" @click="editor.chain().focus().toggleUnderline().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.underline" /></svg>
        </button>
      </el-tooltip>
      <el-tooltip content="删除线" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('strike') }" @click="editor.chain().focus().toggleStrike().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.strike" /></svg>
        </button>
      </el-tooltip>
      
      <!-- Color Pickers -->
      <div class="color-picker-wrapper">
        <el-tooltip content="字体颜色" placement="bottom" :show-after="500">
          <div class="color-btn">
             <el-color-picker v-model="textColor" show-alpha size="small" @change="setTextColor" />
          </div>
        </el-tooltip>
      </div>
      <div class="color-picker-wrapper">
        <el-tooltip content="背景高亮" placement="bottom" :show-after="500">
           <div class="color-btn">
             <el-color-picker v-model="highlightColor" show-alpha size="small" @change="setHighlightColor" />
           </div>
        </el-tooltip>
      </div>
    </div>

    <div class="divider"></div>

    <!-- Group 4: Alignment & Lists -->
    <div class="toolbar-group">
       <el-dropdown trigger="click" @command="handleAlign">
        <button class="toolbar-btn dropdown-btn">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="currentAlignIcon" /></svg>
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="left" :class="{ 'is-active': editor.isActive({ textAlign: 'left' }) }">
               <div class="flex-row">
                 <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path :d="icons.alignLeft" /></svg> 左对齐               </div>
            </el-dropdown-item>
            <el-dropdown-item command="center" :class="{ 'is-active': editor.isActive({ textAlign: 'center' }) }">
               <div class="flex-row">
                 <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path :d="icons.alignCenter" /></svg> 居中对齐
               </div>
            </el-dropdown-item>
            <el-dropdown-item command="right" :class="{ 'is-active': editor.isActive({ textAlign: 'right' }) }">
               <div class="flex-row">
                 <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path :d="icons.alignRight" /></svg> 右对齐               </div>
            </el-dropdown-item>
            <el-dropdown-item command="justify" :class="{ 'is-active': editor.isActive({ textAlign: 'justify' }) }">
               <div class="flex-row">
                 <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path :d="icons.alignJustify" /></svg> 两端对齐
               </div>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <el-tooltip content="无序列表" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('bulletList') }" @click="editor.chain().focus().toggleBulletList().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.bulletList" /></svg>
        </button>
      </el-tooltip>
      <el-tooltip content="有序列表" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('orderedList') }" @click="editor.chain().focus().toggleOrderedList().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.orderedList" /></svg>
        </button>
      </el-tooltip>
      <el-tooltip content="任务列表" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('taskList') }" @click="editor.chain().focus().toggleTaskList().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.taskList" /></svg>
        </button>
      </el-tooltip>
    </div>

    <div class="divider"></div>

    <!-- Group 5: Insert -->
    <div class="toolbar-group">
      <el-tooltip content="引用" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('blockquote') }" @click="editor.chain().focus().toggleBlockquote().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.blockquote" /></svg>
        </button>
      </el-tooltip>
      <el-tooltip content="代码块" placement="bottom" :show-after="500">
        <button class="toolbar-btn" :class="{ 'is-active': editor.isActive('codeBlock') }" @click="editor.chain().focus().toggleCodeBlock().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.codeBlock" /></svg>
        </button>
      </el-tooltip>
      <el-tooltip content="分割线" placement="bottom" :show-after="500">
        <button class="toolbar-btn" @click="editor.chain().focus().setHorizontalRule().run()">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.horizontalRule" /></svg>
        </button>
      </el-tooltip>
      <el-tooltip content="插入图片" placement="bottom" :show-after="500">
        <button class="toolbar-btn" @click="triggerImageUpload">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path :d="icons.image" /></svg>
        </button>
      </el-tooltip>
      <input type="file" ref="fileInput" style="display: none" accept="image/*" @change="handleImageUpload">
    </div>

    <div class="divider"></div>

    <div class="toolbar-group">
      <el-tooltip :content="spellcheckTooltip" placement="bottom" :show-after="500">
        <button
          class="toolbar-btn toolbar-toggle-btn"
          :class="{ 'is-active': spellcheckEnabled }"
          @click="$emit('toggle-spellcheck')">
          <span class="toolbar-token">ABC</span>
          <span class="btn-text">拼写</span>
        </button>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { uploadFile } from '@/views/article/_domain/article.js';
import { ArrowDown } from '@element-plus/icons-vue';
import { icons } from './icons.js';

const props = defineProps({
  editor: {
    type: Object,
    required: true,
  },
  tocVisible: {
    type: Boolean,
    default: true
  },
  spellcheckEnabled: {
    type: Boolean,
    default: false
  }
});

defineEmits(['toggle-toc', 'toggle-spellcheck']);

const fileInput = ref(null);
const textColor = ref('#000000');
const highlightColor = ref('#FFFFFF');

// Update colors when selection changes
watch(() => props.editor.state.selection, () => {
  const textStyle = props.editor.getAttributes('textStyle');
  const highlight = props.editor.getAttributes('highlight');
  textColor.value = textStyle.color || '#000000';
  highlightColor.value = highlight.color || '#FFFFFF'; 
});

const currentHeadingLabel = computed(() => {
  if (props.editor.isActive('heading', { level: 1 })) return 'H1';
  if (props.editor.isActive('heading', { level: 2 })) return 'H2';
  if (props.editor.isActive('heading', { level: 3 })) return 'H3';
  if (props.editor.isActive('heading', { level: 4 })) return 'H4';
  if (props.editor.isActive('heading', { level: 5 })) return 'H5';
  if (props.editor.isActive('heading', { level: 6 })) return 'H6';
  return '正文';
});

const currentAlignIcon = computed(() => {
  if (props.editor.isActive({ textAlign: 'center' })) return icons.alignCenter;
  if (props.editor.isActive({ textAlign: 'right' })) return icons.alignRight;
  if (props.editor.isActive({ textAlign: 'justify' })) return icons.alignJustify;
  return icons.alignLeft;
});

const spellcheckTooltip = computed(() => (
  props.spellcheckEnabled ? '关闭拼写检查' : '开启拼写检查'
));

const handleHeading = (command) => {
  if (command === 'p') {
    props.editor.chain().focus().setParagraph().run();
  } else {
    props.editor.chain().focus().toggleHeading({ level: parseInt(command) }).run();
  }
};

const handleAlign = (align) => {
  props.editor.chain().focus().setTextAlign(align).run();
};

const setTextColor = (color) => {
  if (color) {
    props.editor.chain().focus().setColor(color).run();
  } else {
    props.editor.chain().focus().unsetColor().run();
  }
};

const setHighlightColor = (color) => {
  if (color) {
    props.editor.chain().focus().toggleHighlight({ color: color }).run();
  } else {
    props.editor.chain().focus().unsetHighlight().run();
  }
};

const triggerImageUpload = () => {
  fileInput.value.click();
};

const handleImageUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10MB');
    return;
  }

  try {
    ElMessage.info('正在上传图片...');
    const res = await uploadFile(file);
    if (res && res.url) {
      props.editor.chain().focus().setImage({ src: res.url }).run();
      ElMessage.success('图片上传成功');
    } else {
      ElMessage.error('图片上传失败');
    }
  } catch (error) {
    console.error('Upload error:', error);
    ElMessage.error('图片上传出错');
  } finally {
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
  margin: 0 4px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  color: #4b5563;
  transition: all 0.2s;
  font-size: 14px;
  padding: 0 4px;
}

.toolbar-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: #111827;
}

.toolbar-btn.is-active {
  background-color: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.toolbar-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.dropdown-btn {
  gap: 4px;
  padding: 0 8px;
  width: auto;
}

.toolbar-toggle-btn {
  gap: 6px;
  padding: 0 10px;
  width: auto;
  border-radius: 999px;
}

.btn-text {
  font-size: 12px;
  font-weight: 500;
}

.toolbar-token {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 18px;
  padding: 0 4px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: inherit;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.toolbar-btn.is-active .toolbar-token {
  background: rgba(59, 130, 246, 0.16);
}

.flex-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Color Picker Customization */
.color-picker-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
}

:deep(.el-color-picker__trigger) {
  width: 24px;
  height: 24px;
  padding: 2px;
  border: none;
}

:deep(.el-dropdown-menu__item.is-active) {
  color: #3b82f6;
  background-color: rgba(59, 130, 246, 0.05);
}

@media (max-width: 768px) {
  .tiptap-toolbar {
    flex-wrap: nowrap;
    overflow-x: auto;
    width: auto;
    padding-bottom: 2px;
  }
  
  .tiptap-toolbar::-webkit-scrollbar {
    display: none;
  }
}
</style>
