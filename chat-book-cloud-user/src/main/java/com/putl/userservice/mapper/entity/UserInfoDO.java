package com.putl.userservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.putl.userservice.common.enums.RoleEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * [UserInfo]实体类  用户信息表
 *
 * @author putl
 * @since 2024-12-29 17:36:33
 */

@Data
@TableName("user_info")
@Builder
public class UserInfoDO {
    @TableId(value = "id", type = IdType.AUTO)
    //用户信息表id
    private Integer id;

    //用户id
    private Integer userId;

    //用户名
    private String username;

    //用户头像、照片
    private String photo;

    //个人简介
    private String profile;

    //角色(0普通用户，1管理员)
    private RoleEnum role;

    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //最后一次修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}

