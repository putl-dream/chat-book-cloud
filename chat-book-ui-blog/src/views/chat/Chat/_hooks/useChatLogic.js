import { ref } from 'vue';
import { ElMessage } from "element-plus";
import { getFriendList } from "@/api/social.js";
import { getChatHistory, getUnreadCount, markAsRead } from "@/api/chat.js";
import { API_CONFIG } from "@/config/index.js";
import SocketService, { formatWsUrl } from "@/utils/websocket.js";

export function useChatLogic() {
  const friends = ref([]);
  const messages = ref([]);
  const selectedFriend = ref(null);
  const messageList = ref(null);
  const newMessage = ref('');
  let socketService = null;

  const friendsListRequest = async () => {
    const res = await getFriendList();
    if (res) {
      friends.value = res;
    }
  };

  const selectFriend = async (friend) => {
    if (selectedFriend.value?.userId === friend.userId) return;
    messages.value = [];
    selectedFriend.value = friend;
    // 调用新的 chat-service API
    const res = await getChatHistory(selectedFriend.value.userId, 1, 50);
    if (res && res.data) {
      // P0 Fix: 以当前登录用户的 userId 为基准判断发送方
      // 原代码错误：senderId === selectedFriend.userId 判断为 self，实为对方发的
      const currentUserId = parseInt(localStorage.getItem('userId'));
      for (let i = 0; i < res.data.length; i++) {
        if (res.data[i].senderId === currentUserId) {
          // 发送者是自己
          messages.value.push({
            sender: 'self',
            content: res.data[i].content,
            avatar: localStorage.getItem('avatar'),
          });
        } else {
          // 发送者是对方（selectedFriend）
          messages.value.push({
            sender: 'other',
            content: res.data[i].content,
            avatar: selectedFriend.value.photo,
          });
        }
      }
    }
  };

  const scrollToBottom = () => {
    if (messageList.value) {
      messageList.value.scrollTop = messageList.value.scrollHeight;
    }
  };

  const connectWebSocket = () => {
    const token = localStorage.getItem('token');
    let wsUrl = formatWsUrl(API_CONFIG.baseURL);

    socketService = new SocketService(`${wsUrl}/api/chat/ws`, token);

    socketService.onOpen(() => {
      console.log('已连接到服务器');
      sendSystemMessage();
    });

    socketService.on('SYSTEM', (data) => {
      ElMessage.success(data);
    });

    socketService.on('USER', (data) => {
      messages.value.push({
        sender: 'other',
        content: data.content,
        avatar: selectedFriend.value?.photo,
      });
      scrollToBottom();
    });

    socketService.onClose(() => {
      console.log('已断开与服务器的连接');
    });

    socketService.onError((error) => {
      console.log('错误: ' + error.message);
    });

    socketService.connect();
  };

  const sendMessage = () => {
    if (!newMessage.value.trim()) {
      return ElMessage.warning('请输入内容');
    }
    if (!selectedFriend.value) {
      return ElMessage.warning('请选择好友');
    }
    sendUserMessageBox();
    sendUserMessage();
  };

  const sendSystemMessage = () => {
    if (socketService && socketService.isConnected()) {
      socketService.send("SYSTEM", null);
    }
  };

  const sendUserMessageBox = () => {
    messages.value.push({
      sender: 'self',
      content: newMessage.value,
      avatar: localStorage.getItem('avatar'),
    });
    scrollToBottom();
  };

  const sendUserMessage = () => {
    if (socketService && socketService.isConnected()) {
      socketService.send("USER", { receiverId: selectedFriend.value.userId, content: newMessage.value });
      newMessage.value = '';
    } else {
      ElMessage.error('消息发送失败，连接已断开');
    }
  };

  const initChat = async () => {
    await friendsListRequest();
    connectWebSocket();
  };

  const destroyChat = () => {
    if (socketService) {
      socketService.close();
    }
  };

  return {
    friends,
    messages,
    selectedFriend,
    messageList,
    newMessage,
    friendsListRequest,
    selectFriend,
    scrollToBottom,
    connectWebSocket,
    sendMessage,
    sendSystemMessage,
    sendUserMessageBox,
    sendUserMessage,
    initChat,
    destroyChat
  };
}
