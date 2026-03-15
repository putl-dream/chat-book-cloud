create table user_follow
(
    id          int auto_increment
        primary key,
    user_id     int                                not null comment '关注者用户ID',
    follow_id   int                                not null comment '被关注者用户ID',
    status      int      default 0                 null comment '状态：0-关注 1-好友 2-拉黑',
    create_time datetime default CURRENT_TIMESTAMP null comment '关注时间',
    update_time datetime                           null,
    deleted     tinyint  default 0                 null comment '逻辑删除',
    constraint uk_follow
        unique (user_id, follow_id)
)
    comment '用户关注关系表' charset = utf8mb4;

create index idx_follow_id
    on user_follow (follow_id);

create index idx_user_id
    on user_follow (user_id);
