<template>
    <div class="websocket-client">
        <h1>WebSocket 文本编辑器</h1>
        <textarea v-model="messageInput" @input="onInput" placeholder="输入文章..." class="message-input"></textarea>
    </div>
</template>

<script setup>
import {ref, onMounted, onUnmounted} from 'vue';
import {useRoute} from "vue-router";

const messages = ref([]);
const messageInput = ref('');
let socket;
let debounceTimeout;

// 创建WebSocket连接
const connect = () => {
    const uri = 'ws://localhost:8080/user/chat';
    socket = new WebSocket(uri);

    socket.onopen = function (event) {
        logMessage('已连接到服务器');
    };

    socket.onmessage = function (event) {
        logMessage('收到消息: ' + event.data);
    };

    socket.onclose = function (event) {
        logMessage('已断开与服务器的连接');
    };

    socket.onerror = function (error) {
        logMessage('错误: ' + error.message);
    };
};

// 发送消息
const sendMessage = () => {
    const message = messageInput.value;
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(message);
        logMessage('已发送: ' + message);
    } else {
        logMessage('WebSocket 未打开。当前状态: ' + socket.readyState);
    }
};

// 日志记录
const logMessage = (message) => {
    messages.value.push(message);
};

// 处理输入事件
const onInput = () => {
    clearTimeout(debounceTimeout);
    debounceTimeout = setTimeout(() => {
        sendMessage();
    }, 1000); // 1秒后发送数据
};


// 绑定按钮点击事件
onMounted(() => {
    connect();
});

onUnmounted(() => {
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.close();
    }
});
</script>

<style scoped>
.websocket-client {
    font-family: Arial, sans-serif;
    margin: 20px;
}

.message-input {
    width: 100%;
    height: 200px;
    padding: 10px;
    margin-bottom: 10px;
}
</style>
