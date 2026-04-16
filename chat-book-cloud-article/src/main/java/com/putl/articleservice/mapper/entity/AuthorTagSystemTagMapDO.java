package com.putl.articleservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("author_tag_system_tag_map")
public class AuthorTagSystemTagMapDO implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer authorTagId;

    private Integer systemTagId;

    private String source;

    private BigDecimal confidence;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
