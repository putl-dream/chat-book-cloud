package com.putl.articleservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("mcp_token")
public class McpTokenDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String name;

    private String tokenHash;

    private String scopes;

    private String status;

    private LocalDateTime lastUsedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
