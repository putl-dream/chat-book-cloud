create table user
(
    id          int auto_increment
        primary key,
    email       varchar(255) default ''                not null comment '邮箱',
    password    varchar(120) default ''                not null,
    status      tinyint      default 0                 not null comment '账号状态 0-正常 1-禁用',
    create_time datetime     default CURRENT_TIMESTAMP not null,
    update_time datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint user_pk_2
        unique (email, password)
)
    comment '用户表';

create table user_info
(
    id          int auto_increment comment '用户信息表id'
        primary key,
    user_id     int          default 0                                                                            not null comment '用户id',
    username    varchar(64)  default ''                                                                           not null comment '用户名',
    photo       varchar(255) default 'https://haowallpaper.com/link/common/file/getCroppingImg/15539078305452352' not null comment '用户头像、照片',
    profile     varchar(255) default '这个人很懒，什么也没写'                                                      not null comment '个人简介',
    role        tinyint      default 0                                                                            not null comment '角色(0普通用户，1管理员)',
    create_time datetime     default (now())                                                                      not null comment '创建时间',
    update_time datetime     default (now())                                                                      not null comment '最后一次修改时间'
)
    comment '用户信息表' row_format = DYNAMIC;

create unique index uk_user_info_user_id
    on user_info (user_id);

create unique index uk_user_info_username
    on user_info (username);

create table admin_operation_log
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    operator_id   int                                    null comment '操作人ID',
    operator_name varchar(64)  default ''                not null comment '操作人名称',
    action        varchar(64)  default ''                not null comment '操作类型',
    target_type   varchar(32)  default ''                not null comment '对象类型',
    target_id     int                                    null comment '对象ID',
    detail        text                                   null comment '操作详情JSON',
    ip            varchar(64)  default ''                not null comment '操作人IP',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    key idx_admin_operation_log_operator_id (operator_id),
    key idx_admin_operation_log_target (target_type, target_id),
    key idx_admin_operation_log_action (action),
    key idx_admin_operation_log_create_time (create_time)
)
    comment '管理员操作日志表' row_format = DYNAMIC;
