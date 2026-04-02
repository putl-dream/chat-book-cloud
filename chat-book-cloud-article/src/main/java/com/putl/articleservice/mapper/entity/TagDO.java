package com.putl.articleservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签实体类
 */
@Data
@Builder
@TableName("tag")
@AllArgsConstructor
@NoArgsConstructor
public class TagDO implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    /**
     * 标签类型：1-技术栈 2-学习路径 3-主题标签
     */
    private Integer type;

    private String color;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
