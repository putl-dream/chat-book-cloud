<template>
    <div class="chat-page-wrapper">
        <div class="chat-container glass-effect">
            <el-row class="chat-layout">
                <!-- 好友列表 -->
                <el-col :span="7" class="friend-list-sidebar">
                    <div class="sidebar-header">
                        <h2 class="sidebar-title">
                            <span class="text-gradient">消息列表</span>
                        </h2>
                        <div class="sidebar-actions">
                            <el-button icon="Plus" circle size="small" class="glass-btn"></el-button>
                        </div>
                    </div>
                    <div class="friend-list-scroll custom-scrollbar">
                        <div v-for="friend in friends" :key="friend.id" class="friend-item-wrapper"
                            :class="{ 'is-active': selectedFriend?.userId === friend.userId }"
                            @click="selectFriend(friend)">
                            <ChatUserCard :friend="friend" :active="selectedFriend?.userId === friend.userId" />
                        </div>
                    </div>
                </el-col>

                <!-- 对话框 -->
                <el-col :span="17" class="chat-main-area">
                    <template v-if="selectedFriend">
                        <div class="chat-header glass-header">
                            <div class="user-meta">
                                <div class="avatar-wrapper">
                                    <el-avatar :size="42" :src="selectedFriend.photo" class="user-avatar" />
                                    <span class="status-dot"></span>
                                </div>
                                <div class="user-info">
                                    <span class="user-name">{{ selectedFriend.username }}</span>
                                    <span class="user-status">在线</span>
                                </div>
                            </div>
                            <div class="header-actions">
                                <el-button icon="Phone" circle plain class="action-btn"></el-button>
                                <el-button icon="VideoCamera" circle plain class="action-btn"></el-button>
                                <el-button icon="More" circle plain class="action-btn"></el-button>
                            </div>
                        </div>

                        <div class="message-list-viewport custom-scrollbar" ref="messageList">
                            <div class="message-list-content">
                                <transition-group name="message-fade">
                                    <div v-for="(message, index) in messages" :key="index" class="message-item-anim">
                                        <ChatMessage :message="message" />
                                    </div>
                                </transition-group>
                            </div>
                        </div>

                        <div class="input-area-wrapper">
                            <div class="input-container glass-panel">
                                <div class="toolbar">
                                    <el-button icon="Picture" link class="tool-btn"></el-button>
                                    <el-button icon="Folder" link class="tool-btn"></el-button>
                                    <el-button icon="Microphone" link class="tool-btn"></el-button>
                                    <el-button icon="Star" link class="tool-btn"></el-button>
                                </div>
                                <textarea v-model="newMessage" maxlength="500" placeholder="Type a message..."
                                    class="message-textarea custom-scrollbar"
                                    @keydown.enter.prevent="sendMessage"></textarea>
                                <div class="input-footer">
                                    <span class="char-count">{{ newMessage.length }}/500</span>
                                    <el-button type="primary" class="send-btn" @click="sendMessage">
                                        Send
                                        <el-icon class="el-icon--right">
                                            <Promotion />
                                        </el-icon>
                                    </el-button>
                                </div>
                            </div>
                        </div>
                    </template>
                    <div v-else class="empty-state">
                        <div class="empty-content">
                            <div class="empty-icon-wrapper">
                                <el-icon :size="80" class="empty-icon">
                                    <ChatDotRound />
                                </el-icon>
                            </div>
                            <h3>欢迎回来</h3>
                            <p>选择一个好友开始聊天</p>
                        </div>
                    </div>
                </el-col>
            </el-row>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from "element-plus";
import ChatUserCard from "@/components/widget/ChatUserCard.vue";
import ChatMessage from "@/components/widget/ChatMsgCard.vue";
import { getFriendList, queryUserMessage } from "@/api/user.js";

const friends = ref([])
const messages = ref([])
const selectedFriend = ref(null)
const messageList = ref(null)

const friendsListRequest = async () => {
    const res = await getFriendList()
    if (res) {
        friends.value = res
        console.log(res)
    }
}

const selectFriend = async (friend) => {
    if (selectedFriend.value?.userId === friend.userId) return
    messages.value = []
    selectedFriend.value = friend
    console.log("选择用户：", friend)
    const res = await queryUserMessage(selectedFriend.value.userId);
    if (res) {
        console.log("查询消息：", res)
        for (let i = 0; i < res.length; i++) {
            if (res[i].senderId === selectedFriend.value.userId) {
                messages.value.push({
                    sender: 'self',
                    content: res[i].content,
                    avatar: localStorage.getItem('avatar'),
                })
            } else if (res[i].receiverId === selectedFriend.value.userId) {
                messages.value.push({
                    sender: 'other',
                    content: res[i].content,
                    avatar: selectedFriend.value.photo,
                })
            }
        }
        await nextTick(() => {
            scrollToBottom()
        })
    }
}

const scrollToBottom = () => {
    if (messageList.value) {
        messageList.value.scrollTop = messageList.value.scrollHeight;
    }
}

// WebSocket 相关
let socket;
const newMessage = ref('')
const connectWebSocket = () => {
    const token = localStorage.getItem('token');
    socket = new WebSocket('ws://localhost:8080/user/chat', [token]);

    socket.onopen = function (event) {
        console.log('已连接到服务器');
        sendSystemMessage()
    };

    socket.onmessage = function (event) {
        const parse = JSON.parse(event.data)
        console.log("接收到消息-->>", parse)
        if (parse.type === 'SYSTEM') {
            console.log("系统消息-->>", parse.data)
            ElMessage.success(parse.data)
        } else if (parse.type === 'USER') {
            console.log("用户消息-->>", parse.data)
            messages.value.push({
                sender: 'other',
                content: parse.data.content,
                avatar: selectedFriend.value?.photo,
            })
            nextTick(() => {
                scrollToBottom()
            })
        }
    }

    socket.onclose = function (event) {
        console.log('已断开与服务器的连接');
    };

    socket.onerror = function (error) {
        console.log('错误: ' + error.message);
    };
}

const sendMessage = () => {
    if (!newMessage.value.trim()) {
        return ElMessage.warning('请输入内容')
    }
    if (!selectedFriend.value) {
        return ElMessage.warning('请选择好友')
    }
    sendUserMessageBox()
    sendUserMessage()
};

const sendSystemMessage = () => {
    const messageJSON = JSON.stringify({ type: "SYSTEM", data: null });
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(messageJSON);
    }
};

const sendUserMessageBox = () => {
    messages.value.push({
        sender: 'self',
        content: newMessage.value,
        avatar: localStorage.getItem('avatar'),
    })
    nextTick(() => {
        scrollToBottom()
    })
};

const sendUserMessage = () => {
    const messageJSON = JSON.stringify({
        type: "USER",
        data: { receiverId: selectedFriend.value.userId, content: newMessage.value }
    });
    if (socket && socket.readyState === WebSocket.OPEN) {
        newMessage.value = ''
        socket.send(messageJSON);
    } else {
        ElMessage.error('消息发送失败，连接已断开')
    }
};

onMounted(() => {
    friendsListRequest()
    connectWebSocket()
})
</script>

<style scoped>
.chat-page-wrapper {
    height: 100%;
    padding: 24px;
    background: var(--bg-color-base);
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
    /* Subtle animated gradient background */
    background: radial-gradient(circle at top left, #f0f4ff, var(--bg-color-base));
}

.chat-container {
    width: 100%;
    max-width: 1200px;
    height: 90vh;
    border-radius: 24px;
    border: 1px solid rgba(255, 255, 255, 0.6);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    background: rgba(255, 255, 255, 0.65);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    display: flex;
    flex-direction: column;
}

.chat-layout {
    height: 100%;
    margin: 0 !important;
}

/* Sidebar Styling */
.friend-list-sidebar {
    height: 100%;
    display: flex;
    flex-direction: column;
    border-right: 1px solid rgba(255, 255, 255, 0.3);
    background: rgba(255, 255, 255, 0.4);
}

.sidebar-header {
    padding: 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.sidebar-title {
    margin: 0;
    font-size: 24px;
    font-weight: 800;
    letter-spacing: -0.5px;
}

.text-gradient {
    background: linear-gradient(135deg, var(--color-primary) 0%, #a0c4ff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.glass-btn {
    background: rgba(255, 255, 255, 0.5);
    border: 1px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
}

.glass-btn:hover {
    background: #fff;
    transform: translateY(-2px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.friend-list-scroll {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
}

.friend-item-wrapper {
    margin-bottom: 8px;
    border-radius: 16px;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    cursor: pointer;
    border: 1px solid transparent;
}

.friend-item-wrapper:hover {
    background: rgba(255, 255, 255, 0.4);
    transform: translateX(4px);
}

.friend-item-wrapper.is-active {
    background: rgba(255, 255, 255, 0.8);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.05);
    border-color: rgba(255, 255, 255, 0.9);
}

/* Chat Main Area */
.chat-main-area {
    height: 100%;
    display: flex;
    flex-direction: column;
    background: rgba(255, 255, 255, 0.3);
    position: relative;
}

.chat-header {
    padding: 16px 32px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid rgba(255, 255, 255, 0.3);
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(10px);
    z-index: 10;
}

.avatar-wrapper {
    position: relative;
    padding: 2px;
    border: 2px solid rgba(255, 255, 255, 0.8);
    border-radius: 50%;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
}

.status-dot {
    position: absolute;
    bottom: 2px;
    right: 2px;
    width: 10px;
    height: 10px;
    background: #4cc9f0;
    border: 2px solid #fff;
    border-radius: 50%;
    box-shadow: 0 0 0 2px rgba(76, 201, 240, 0.2);
}

.user-info {
    display: flex;
    flex-direction: column;
    margin-left: 12px;
}

.user-name {
    font-weight: 700;
    font-size: 16px;
    color: var(--text-color-primary);
}

.user-status {
    font-size: 12px;
    color: var(--text-color-secondary);
    font-weight: 500;
}

.action-btn {
    border: none;
    background: transparent;
    color: var(--text-color-secondary);
    transition: all 0.2s;
}

.action-btn:hover {
    background: rgba(0, 0, 0, 0.05);
    color: var(--color-primary);
}

/* Messages Area */
.message-list-viewport {
    flex: 1;
    overflow-y: auto;
    padding: 32px;
}

.message-item-anim {
    margin-bottom: 16px;
}

/* Message Entrance Animation */
.message-fade-enter-active,
.message-fade-leave-active {
    transition: all 0.4s ease;
}

.message-fade-enter-from,
.message-fade-leave-to {
    opacity: 0;
    transform: translateY(20px);
}

/* Input Area */
.input-area-wrapper {
    padding: 24px 32px 32px;
}

.input-container {
    background: #fff;
    border-radius: 20px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
    padding: 16px 24px;
    border: 1px solid rgba(255, 255, 255, 0.5);
    transition: all 0.3s ease;
}

.input-container:focus-within {
    box-shadow: 0 15px 40px rgba(var(--color-primary-rgb), 0.1);
    transform: translateY(-2px);
    border-color: var(--color-primary-light);
}

.toolbar {
    display: flex;
    gap: 12px;
    margin-bottom: 8px;
}

.tool-btn {
    color: var(--text-color-secondary);
    transition: color 0.2s;
}

.tool-btn:hover {
    color: var(--color-primary);
}

.message-textarea {
    width: 100%;
    height: 60px;
    border: none;
    outline: none;
    resize: none;
    background: transparent;
    font-family: inherit;
    font-size: 15px;
    color: var(--text-color-primary);
    line-height: 1.5;
}

.input-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px solid var(--border-color-lighter);
}

.char-count {
    font-size: 12px;
    color: var(--text-color-placeholder);
    font-weight: 500;
}

.send-btn {
    border-radius: 12px;
    padding: 8px 24px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
    transition: all 0.3s;
}

.send-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(var(--color-primary-rgb), 0.4);
}

/* Empty State */
.empty-state {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgba(255, 255, 255, 0.3);
}

.empty-content {
    text-align: center;
    color: var(--text-color-secondary);
}

.empty-icon-wrapper {
    width: 120px;
    height: 120px;
    background: linear-gradient(135deg, #e0eaff 0%, #fff 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
}

.empty-icon {
    color: var(--color-primary);
    opacity: 0.8;
}

.empty-content h3 {
    font-size: 24px;
    margin-bottom: 8px;
    color: var(--text-color-primary);
    font-weight: 700;
}

/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.2);
}
</style>