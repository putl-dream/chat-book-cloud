package com.putl.chatservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.chatservice.entity.MessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息 Mapper 接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<MessageDO> {

    /**
     * 查询两个用户之间的历史消息
     */
    @Select("SELECT * FROM message " +
            "WHERE ((sender_id = #{userId1} AND receiver_id = #{userId2}) " +
            "   OR (sender_id = #{userId2} AND receiver_id = #{userId1})) " +
            "AND is_deleted = 0 " +
            "ORDER BY sent_time DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<MessageDO> selectHistoryMessages(@Param("userId1") Integer userId1,
                                           @Param("userId2") Integer userId2,
                                           @Param("limit") Integer limit,
                                           @Param("offset") Integer offset);

    /**
     * 统计用户未读消息数
     */
    @Select("SELECT COUNT(*) FROM message " +
            "WHERE receiver_id = #{userId} " +
            "AND status = 1 " +
            "AND is_deleted = 0")
    Integer countUnreadMessages(@Param("userId") Integer userId);

    /**
     * 标记消息为已读
     */
    @Select("UPDATE message SET status = 2, read_time = NOW() " +
            "WHERE receiver_id = #{userId} " +
            "AND sender_id = #{targetUserId} " +
            "AND status = 1 " +
            "AND is_deleted = 0")
    void markMessagesAsRead(@Param("userId") Integer userId, @Param("targetUserId") Integer targetUserId);
}
