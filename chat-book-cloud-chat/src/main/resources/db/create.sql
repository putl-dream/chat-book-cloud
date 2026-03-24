create table message
(
    id          int auto_increment comment '消息ID'
        primary key,
    sender_id   int                                   not null comment '发送者用户ID',
    receiver_id int                                   not null comment '接收者用户ID',
    content     text                                  not null comment '消息内容',
    msg_type    varchar(20) default 'TEXT'            not null comment '消息类型: TEXT/IMAGE/FILE',
    status      tinyint     default 0                 not null comment '消息状态: 0=已发送, 1=未读, 2=已读',
    sent_time   datetime    default CURRENT_TIMESTAMP not null comment '发送时间',
    read_time   datetime                              null comment '读取时间',
    is_deleted  tinyint     default 0                 not null comment '是否删除: 0=否, 1=是'
)
    comment '即时通讯消息表' collate = utf8mb4_unicode_ci;

create index idx_receiver
    on message (receiver_id);

create index idx_receiver_status
    on message (receiver_id, status);

create index idx_sender
    on message (sender_id);

create index idx_sent_time
    on message (sent_time);
