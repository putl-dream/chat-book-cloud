create table user
(
    id          int auto_increment
        primary key,
    email       varchar(255) default ''                not null comment '邮箱',
    password    varchar(120) default ''                not null,
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

create index user_info_user_id_fk
    on user_info (user_id);
