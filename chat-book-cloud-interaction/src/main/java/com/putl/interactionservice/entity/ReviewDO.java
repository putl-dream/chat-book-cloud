package com.putl.interactionservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class ReviewDO {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer textId;
    private Integer userId;
    private Integer parentId;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 评论状态 0-正常 1-已删除 2-已屏蔽 */
    private Integer status;
}
