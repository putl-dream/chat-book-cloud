package com.putl.userservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员操作日志
 */
@Data
@TableName("admin_operation_log")
@Builder
public class AdminOperationLogDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人ID */
    private Integer operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 操作类型 */
    private String action;

    /** 对象类型 */
    private String targetType;

    /** 对象ID */
    private Integer targetId;

    /** 操作详情JSON */
    private String detail;

    /** 操作人IP */
    private String ip;

    /** 创建时间 */
    private LocalDateTime createTime;
}
