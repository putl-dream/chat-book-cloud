import { ref } from 'vue';

export function useSidebarAI() {
    const aiMessage = ref('');
    const aiChatHistory = ref([
        { role: 'ai', content: '你好！我是这篇文章的AI助手，有什么可以帮你的吗？' }
    ]);

    const sendAiMessage = () => {
        if (!aiMessage.value.trim()) return;
        aiChatHistory.value.push({ role: 'user', content: aiMessage.value });
        const userMsg = aiMessage.value;
        aiMessage.value = '';

        // 模拟AI回复
        setTimeout(() => {
            aiChatHistory.value.push({ role: 'ai', content: `针对"${userMsg}"，我觉得这篇文章写得很有深度... (AI功能开发中)` });
        }, 1000);
    };

    return {
        aiMessage,
        aiChatHistory,
        sendAiMessage
    };
}
