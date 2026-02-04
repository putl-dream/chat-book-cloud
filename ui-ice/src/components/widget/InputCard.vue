<template>
    <div class="input-card">
        <!-- 输入框 -->
        <div style="height: 170px; position: relative">
        <textarea
          v-model="message"
          maxlength="500"
          placeholder="请输入..."
          class="message-input"
          @keydown.enter.prevent="sendMessage"
        ></textarea>
            <button class="send-button"  @click="sendMessage">发送</button>
        </div>
    </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'

// 输入框内容
const message = ref('')

// 发送消息
const sendMessage = () => {
    if (!message.value.trim()) {
        alert('请输入消息')
        return
    }

    // 这里可以调用发送消息的逻辑
    console.log('发送消息:', message.value)
    message.value = ''
}
</script>

<style scoped>
.input-card {
    align-items: center;
    border-radius: 4px;
    height: 60px;
}

.message-input {
    background-color: transparent;
    border: none;
    outline: none;
    resize: none;
    width: 100%;
    height: 100%;
    padding: 10px;
    box-sizing: border-box;
    font-size: 16px;
    color: #333;
}

.append-text {
    position: absolute;
    right: 20px;
    top: 50%;
    transform: translateY(-50%);
    color: #999;
}

.send-button {
    position: absolute;
    right: 20px;
    bottom: 0;
    background-color: #ff8940;
    color: white;
    border: none;
    border-radius: 2px;
    padding: 8px 15px;
    cursor: pointer;
    font-size: 14px;
}

.send-button:hover {
    background-color: #3687f0;
}

.message-input::-webkit-scrollbar {
    width: 4px;
}

/*滚动条轨道*/
.message-input::-webkit-scrollbar-track {
    background: rgb(239, 239, 239);
    border-radius: 2px;
}

/*小滑块*/
.message-input::-webkit-scrollbar-thumb {
    background: #40a0ff49;
    border-radius: 10px;
}
</style>
