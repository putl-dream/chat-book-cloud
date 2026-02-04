<template>
    <div class="chat-page-wrapper">
        <div class="chat-container glass-effect">
            <el-row class="chat-layout">
                <!-- 好友列表 -->
                <el-col :span="7" class="friend-list-sidebar">
                    <div class="sidebar-header">
                        <h2 class="sidebar-title">消息列表</h2>
                        <div class="sidebar-actions">
                            <el-button icon="Plus" circle size="small"></el-button>
                        </div>
                    </div>
                    <div class="friend-list-scroll">
                        <div v-for="friend in friends" :key="friend.id" @click="selectFriend(friend)">
                            <ChatUserCard :friend="friend" :active="selectedFriend?.userId === friend.userId" />
                        </div>
                    </div>
                </el-col>

                <!-- 对话框 -->
                <el-col :span="17" class="chat-main-area">
                    <template v-if="selectedFriend">
                        <div class="chat-header">
                            <div class="user-meta">
                                <el-avatar :size="40" :src="selectedFriend.photo" />
                                <div class="user-info">
                                    <span class="user-name">{{ selectedFriend.username }}</span>
                                    <span class="user-status">在线</span>
                                </div>
                            </div>
                            <div class="header-actions">
                                <el-button icon="Phone" circle plain></el-button>
                                <el-button icon="VideoCamera" circle plain></el-button>
                                <el-button icon="More" circle plain></el-button>
                            </div>
                        </div>

                        <div class="message-list-viewport" ref="messageList">
                            <div class="message-list-content">
                                <div v-for="(message, index) in messages" :key="index">
                                    <ChatMessage :message="message" />
                                </div>
                            </div>
                        </div>

                        <div class="input-area-wrapper">
                            <div class="toolbar">
                                <el-button icon="Picture" link></el-button>
                                <el-button icon="Folder" link></el-button>
                                <el-button icon="Microphone" link></el-button>
                                <el-button icon="Star" link></el-button>
                            </div>
                            <div class="input-box">
                                <textarea v-model="newMessage" maxlength="500" placeholder="发个消息聊聊吧..."
                                    class="message-textarea" @keydown.enter.prevent="sendMessage"></textarea>
                                <div class="input-footer">
                                    <span class="char-count">{{ newMessage.length }}/500</span>
                                    <el-button type="primary" class="send-btn" @click="sendMessage">
                                        发送
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
                            <el-icon :size="64" color="var(--text-color-placeholder)">
                                <ChatDotRound />
                            </el-icon>
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
    height: calc(100vh - var(--header-height));
    padding: 24px;
    background: var(--bg-color-base);
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
}

.chat-container {
    width: 100%;
    max-width: var(--container-width-lg);
    height: 100%;
    border-radius: var(--border-radius-xl);
    border: 1px solid rgba(255, 255, 255, 0.4);
    box-shadow: var(--box-shadow-glass);
    overflow: hidden;
    background: var(--bg-color-glass);
}

.glass-effect {
    backdrop-filter: var(--blur-base);
    -webkit-backdrop-filter: var(--blur-base);
}

.chat-layout {
    height: 100%;
}

/* Sidebar Styling */
.friend-list-sidebar {
    height: 100%;
    display: flex;
    flex-direction: column;
    border-right: 1px solid var(--border-color-base);
    background: rgba(255, 255, 255, 0.2);
}

.sidebar-header {
    padding: 20px 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid var(--border-color-light);
}

.sidebar-title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: var(--text-color-primary);
}

.friend-list-scroll {
    flex: 1;
    overflow-y: auto;
    padding: 8px 0;
}

/* Chat Main Area Styling */
.chat-main-area {
    height: 100%;
    display: flex;
    flex-direction: column;
    background: var(--bg-color-white);
}

.chat-header {
    padding: 16px 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid var(--border-color-base);
    background: var(--bg-color-glass);
    backdrop-filter: blur(8px);
    z-index: 10;
}

.user-meta {
    display: flex;
    align-items: center;
    gap: 12px;
}

.user-info {
    display: flex;
    flex-direction: column;
}

.user-name {
    font-weight: 600;
    font-size: 16px;
    color: var(--text-color-primary);
}

.user-status {
    font-size: 12px;
    color: var(--color-success);
}

.message-list-viewport {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
    background: var(--bg-color-base);
    background-image:
        radial-gradient(circle at 2px 2px, var(--border-color-light) 1px, transparent 0);
    background-size: 32px 32px;
}

.message-list-content {
    display: flex;
    flex-direction: column;
}

/* Input Area Styling */
.input-area-wrapper {
    padding: 16px 24px 24px;
    background: var(--bg-color-white);
    border-top: 1px solid var(--border-color-base);
}

.toolbar {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
}

.input-box {
    position: relative;
    border: 1px solid var(--border-color-base);
    border-radius: var(--border-radius-large);
    background: var(--bg-color-light);
    transition: var(--transition-base);
    padding: 12px;
}

.input-box:focus-within {
    border-color: var(--color-primary);
    background: var(--bg-color-white);
    box-shadow: 0 0 0 3px var(--color-primary-light);
}

.message-textarea {
    width: 100%;
    height: 80px;
    border: none;
    outline: none;
    resize: none;
    background: transparent;
    font-family: inherit;
    font-size: 14px;
    color: var(--text-color-primary);
    padding: 0;
}

.input-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;
}

.char-count {
    font-size: 12px;
    color: var(--text-color-placeholder);
}

.send-btn {
    padding: 8px 24px;
    font-weight: 500;
    border-radius: var(--border-radius-base);
}

/* Empty State */
.empty-state {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    background: var(--bg-color-base);
}

.empty-content {
    text-align: center;
    color: var(--text-color-secondary);
}

.empty-content p {
    margin-top: 16px;
    font-size: 16px;
}

/* Scrollbar Styling */
::-webkit-scrollbar {
    width: 6px;
}

::-webkit-scrollbar-track {
    background: transparent;
}

::-webkit-scrollbar-thumb {
    background: var(--border-color-base);
    border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
    background: var(--text-color-placeholder);
}
</style>
