package com.putl.articleservice.ws;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.putl.articleservice.config.SessionConfig;
import com.putl.articleservice.controller.vo.ArticleVO;
import com.putl.articleservice.service.ArticleService;
import com.putl.articleservice.utils.MessageResult;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/article/ws", configurator = SessionConfig.class)
public class ArticleChannel {
    private static final ConcurrentHashMap<Integer, Session> sessions = new ConcurrentHashMap<>();
    private static final HashMap<Integer, String> articleInfo = new HashMap<>();
    private static final String NULL_ARTICLE = "<p><br></p>";
    private Session session;
    private Integer userId;
    private static ArticleService articleService;

    public static void setArticleService(ArticleService articleService){
        ArticleChannel.articleService = articleService;
    }

    // 连接打开
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig){
        session.setMaxBinaryMessageBufferSize(1024 * 1024 * 100);
        session.setMaxTextMessageBufferSize(1024 * 1024 * 100);
        this.session = session;
        HttpSession httpSession = (HttpSession) endpointConfig.getUserProperties().get(HttpSession.class.getName());
        Integer userId = Integer.valueOf(String.valueOf(httpSession.getAttribute("userId")));
        this.userId = userId;

        // 查询是否有缓存信息, 有则发送
        if (articleInfo.containsKey(this.userId)) {
            sendMessage(MessageResult.messageSelect(articleInfo.get(this.userId)));
        }

        sessions.put(this.userId, session);
        log.info("[websocket] 新的连接：用户={} ", userId);
    }

    // 收到消息
    @OnMessage
    public void onMessage(String message) throws IOException{
        log.info("[websocket] 收到消息：用户={}，message={}", this.userId, message);

        // 反序列化
        Message messages = JSONUtil.toBean(message, Message.class);
        if (messages.getType().equals("SAVE") || messages.getType().equals("PUBLISH")){
            if (StringUtils.equals(NULL_ARTICLE, messages.getData().getContent())) {
                sendMessage(MessageResult.messageSave("文章内容为空，不保存！！"));
                return;
            }
        }

        // 缓存文章信息
        articleInfo.put(userId, message);
        sendMessage(MessageResult.messageCache("缓存成功!"));

        // 文章相关操作
        if (!StringUtils.equals("CACHE", messages.type)) {
            articleHandle(messages);
        }

        log.info("[websocket] 收到消息：用户={}，message={}", userId, message);
    }


    /**
     * 连接关闭
     *
     * @param closeReason 关闭原因
     */
    @OnClose
    public void onClose(CloseReason closeReason){
        sessions.remove(userId, this.session);
        articleInfo.remove(userId);
        log.info("[websocket] 连接关闭：用户={}，reason={}", userId, closeReason.getReasonPhrase());
    }

    /**
     * 连接异常
     *
     * @param throwable 异常
     */
    @OnError
    public void onError(Throwable throwable) throws IOException{
        log.info("[websocket] 连接异常：id={}，throwable={}", this.session.getId(), throwable.getMessage());
        // 关闭连接。状态码为 UNEXPECTED_CONDITION（意料之外的异常）
        this.session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, throwable.getMessage()));
    }


    /**
     * 发送消息
     *
     * @param message
     */
    private void sendMessage(String message){
        try {
            this.session.getBasicRemote().sendText(message);
            log.info("<<< 发送消息：id={}，message={}", this.session.getId(), message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * 持久化文章
     * - 默认 ： cache - 缓存
     *  - save 保存
     *  - publish 发布
     *  - select 查询
     */
    private void articleHandle(Message message){
        String article = articleInfo.get(userId);
        log.info("[websocket] 用户={}, 操作{} ,缓存{}", userId, message.getType(), article);
        Integer id = message.getData().getId();
        if (id == null) {
            switch (message.type) {
                case "SAVE":
                    articleService.addArticle(message.getData());
                    sendMessage(MessageResult.messageSave("文章保存至草稿成功！！"));
                    break;
                case "PUBLISH":
                    articleService.addArticle(message.getData());
                    sendMessage(MessageResult.messagePublish("文章发布成功！！"));
                    break;
                default:
                    log.error("[websocket] 用户={} 操作{} 失败", userId, message.getType());
                    break;
            }
        } else {
            switch (message.type) {
                case "SAVE":
                    articleService.updateArticle(message.getData());
                    sendMessage(MessageResult.messageSave("文章保存至草稿成功！！"));
                    break;
                case "PUBLISH":
                    articleService.updateArticle(message.getData());
                    sendMessage(MessageResult.messagePublish("文章发布成功！！"));
                    break;
                case "SELECT":
                    ArticleVO articleVO = articleService.getArticleDetail(id);
                    sendMessage(MessageResult.messageSelect(JSONUtil.toJsonStr(articleVO)));
                    break;
            }
        }


        log.info("[websocket] 用户={} ", userId);
    }

    @Data
    public static class Message {
        private String type;
        private ArticleVO data;
    }
}
