package com.putl.interactionservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("user_foot")
public class UserFootDO {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer documentId;
    private Integer documentType;
    private Integer documentUserId;
    private Integer collectionStat;
    private Integer readStat;
    private Integer commentStat;
    private Integer praiseStat;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
