<template>
    <div class="text-toolbar">
        <CreativeHeader/>
        <div class="toolbar-container">
            <!-- 工具栏 -->
            <el-text v-if="save" style="color: #212121;font-size: 12px">自动保存中...</el-text>
            <el-text v-else style="color: #212121;font-size: 12px">文章已保存...</el-text>
            <Toolbar
                :editor="editor"
                :mode="mode"
            />
        </div>
    </div>

    <div class="main-content">
        <!-- 标题区域 -->
        <div class="title-area">
            <input
                type="text"
                v-model="title"
                placeholder="请输入文章标题"
                class="title-input"
                @input="onInput"
            />
        </div>

        <!-- 内容区域 -->
        <Editor
            class="main-content-editor"
            v-model="html"
            :defaultConfig="editorConfig"
            :mode="mode"
            @onCreated="onCreated"
        />
    </div>
    <div class="footer-card">
        <div class="word-count">
            <el-text>共 {{ wordCount }} 字</el-text>
        </div>
        <div class="buttons">
            <button class="rounded-button" @click="saveContent">保存</button>
            <button class="rounded-button" @click="publishContent">发布</button>
        </div>
    </div>
</template>

<script setup>
import {onMounted, onUnmounted, ref, watch} from 'vue';
import '@wangeditor/editor/dist/css/style.css'; // 引入样式
import {Editor, Toolbar} from '@wangeditor/editor-for-vue';
import CreativeHeader from "@/components/CreativeHeader.vue";
import {ElMessage} from "element-plus";
import {onBeforeRouteUpdate, useRoute} from "vue-router";

// 初始化 wangEditor
let title = ref('');
const editor = ref(null);
const html = ref('');
const mode = ref('simple');
const editorConfig = {
    placeholder: '请输入内容...',
    scroll: false, // 禁用编辑器的滚动条
};

// 初始化 MENU_CONF 对象
if (!editorConfig.MENU_CONF) {
    editorConfig.MENU_CONF = {};
}

editorConfig.MENU_CONF['uploadImage'] = {
    server: 'http://localhost:8080/article/file/upload',
    fieldName: 'file',
    maxFileSize: 1024 * 1024, // 1M
    maxNumberOfFiles: 10,
    allowedFileTypes: ['image/*'],
    meta: {
        token: localStorage.getItem('token'),
    },
    metaWithUrl: false,
    headers: {
        Accept: 'application/json',
        token: localStorage.getItem('token'),
    },
    withCredentials: true,
    timeout: 5 * 1000, // 5 秒
}

function onCreated(editorInstance) {
    editor.value = editorInstance;
}

// 统计字数
function countWords() {
    const parser = new DOMParser();
    const doc = parser.parseFromString(html.value, 'text/html');
    const textContent = doc.body.textContent || '';
    return textContent.replace(/\s+/g, '').length;
}

const wordCount = ref(countWords());
watch(html, () => {
    wordCount.value = countWords();
}, {immediate: true})


// WebSocket 相关
let socket;
let debounceTimeout;
let save = ref(false);

const connectWebSocket = () => {
    const token = localStorage.getItem('token');
    socket = new WebSocket('ws://localhost:8080/article/ws', [token]);

    socket.onopen = function (event) {
        console.log('已连接到服务器');
        if (articleId && articleId !== '') {
            console.log("存在文章ID-->>", articleId, "正在查询")
            sendMessage('SELECT')
        }
    };

    socket.onmessage = function (event) {
        const parse = JSON.parse(event.data)
        messageHandler(parse)
    };

    socket.onclose = function (event) {
        console.log('已断开与服务器的连接');
    };

    socket.onerror = function (error) {
        console.log('错误: ' + error.message);
    };
}
// 消息处理
const messageHandler = (parse) => {
    console.log("消息处理-->>", parse)
    switch (parse.type) {
        case 'USER':
            console.log("用户消息-->>", parse.data)
            break;
        case 'SYSTEM':
            console.log("系统消息-->>", parse.data)
            break;
        case 'CACHE':
            save.value = false;
            break;
        case 'SAVE':
            console.log("保存消息-->>", parse.data)
            ElMessage.success(parse.data)
            window.location.href = '/';
            break;
        case 'PUBLISH':
            console.log("发布消息-->>", parse.data)
            ElMessage.success(parse.data)
            window.location.href = '/';
            break;
        case 'SELECT':
            console.log("查询消息-->>", parse.data)
            html.value = parse.data.content;
            title.value = parse.data.title;
            break;
    }
}

// 监听标题输入事件
let typingTimer = null;
const onInput = () => {
    clearTimeout(typingTimer); // 清除之前的定时器
    typingTimer = setTimeout(() => {
        sendMessage('CACHE')
    }, 1500);
};

// 监听内容输入事件
watch(html, (newVal, oldVal) => {
    clearTimeout(typingTimer); // 清除之前的定时器
    if (newVal !== oldVal && newVal !== '') { // 如果输入框不为空
        save.value = true;
        typingTimer = setTimeout(() => {
            sendMessage('CACHE')
        }, 500);
    }
});

// 保存文章
async function saveContent() {
    ElMessage.success('文章正在保存为草稿！')
    setTimeout(() => {
        sendMessage('SAVE')
    }, 1000);

}

// 发布内容
async function publishContent() {
    // 等待1s
    ElMessage.success('文章正在发布中！')
    setTimeout(() => {
        sendMessage('PUBLISH')
    }, 1000);
}

const sendMessage = (type) => {
    const messageJSON = JSON.stringify({type: type, data: {id: articleId, title: title.value, content: html.value}});
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(messageJSON);
    } else {
        console.log('WebSocket 未打开,当前状态: ' + socket.readyState);
        window.location.reload();
    }
};


// 获取路由参数
const route = useRoute();
const articleId = route.params.id;

// 绑定按钮点击事件
onMounted(() => {
    connectWebSocket();
});

onUnmounted(() => {
    saveContent()
});

// 监听路由变化
onBeforeRouteUpdate((to, from) => {
    if (to.params.id !== from.params.id) {
        console.log("路由变化-->>", to.params.id, from.params.id)
        window.location.reload();
    }
});


</script>

<style scoped>
.text-toolbar {
    position: fixed;
    left: 0;
    right: 0;
    top: 0;
    height: 100px;
    background-color: white;
    z-index: 10;
}

.toolbar-container {
    display: flex;
    justify-content: center;
}

.main-content {
    width: 900px;
    margin: 120px auto 70px;
    padding: 20px;
    background-color: white;
    box-sizing: border-box;
}

.title-area {
    border-bottom: 1px solid #ccc;
    padding: 0 10px 10px 10px;
    margin-bottom: 10px;
}

.main-content-editor {
    min-height: 550px;
    overflow-y: hidden;
}

.footer-card {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: #fff;
    padding: 10px;
    box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.1);
    display: flex;
    justify-content: space-around;
    align-items: center;
    z-index: 10;
}

.word-count {
    font-size: 0.9em;
    color: #999999;
}

.buttons {
    display: flex;
    gap: 10px;
}

.rounded-button {
    padding: 10px 20px;
    border: none;
    border-radius: 5px;
    background-color: #007bff;
    color: #fff;
    cursor: pointer;
}

.rounded-button:hover {
    background-color: #0056b3;
}

.title-input {
    width: 100%;
    padding: 8px;
    box-sizing: border-box;
    font-size: 1.5em;
    border: none; /* 移除边框 */
    outline: none; /* 移除选中时的黑框 */
}
</style>
