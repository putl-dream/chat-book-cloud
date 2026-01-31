package com.putl.userservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * [User]实体类
 *
 * @author putl
 * @since 2024-12-29 17:36:33
 */

@Data
@TableName("user")
@Builder
public class UserDO {
    @TableId(value = "id", type = IdType.AUTO)
    //主键id
    private Integer id;

    //邮箱
    private String email;

    //密码
    private String password;

    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    //最后更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}

