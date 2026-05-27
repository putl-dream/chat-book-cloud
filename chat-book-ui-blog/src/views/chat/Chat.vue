<template>
    <div class="chat-page-wrapper">
        <div class="chat-container glass-effect">
            <el-row class="chat-layout">
                <!-- 好友列表 -->
                <el-col :span="isMobile ? 24 : 7" class="friend-list-sidebar" v-if="!isMobile || !selectedFriend">
                    <div class="sidebar-header">
                        <h2 class="sidebar-title">
                            <span class="u-text-gradient">消息列表</span>
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
                <el-col :span="isMobile ? 24 : 17" class="chat-main-area" v-if="!isMobile || selectedFriend">
                    <template v-if="selectedFriend">
                        <div class="chat-header glass-header">
                            <div class="user-meta">
                                <!-- Mobile Back Button -->
                                <el-button v-if="isMobile" icon="ArrowLeft" circle plain class="action-btn back-btn" @click="selectedFriend = null" style="margin-right: 12px;"></el-button>
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
                                <transition-group name="chat-message-fade">
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
import { onMounted, onUnmounted, ref, nextTick } from 'vue'
import ChatUserCard from "@/views/chat/components/ChatUserCard.vue";
import ChatMessage from "@/views/chat/components/ChatMsgCard.vue";
import { useChatLogic } from "./Chat/_hooks/useChatLogic.js";

const {
  friends,
  messages,
  selectedFriend,
  messageList,
  newMessage,
  selectFriend,
  sendMessage,
  initChat,
  destroyChat
} = useChatLogic();

const isMobile = ref(false);

const checkViewport = () => {
    isMobile.value = window.innerWidth <= 768;
};

onMounted(() => {
    initChat();
    checkViewport();
    window.addEventListener('resize', checkViewport);
})

onUnmounted(() => {
    destroyChat();
    window.removeEventListener('resize', checkViewport);
})
</script>
