create table notification
(
    id          int auto_increment comment '通知ID'
        primary key,
    receiver_id int                                not null comment '接收通知的用户',
    sender_id   int                                not null comment '触发通知的用户',
    action_type varchar(20)                        not null comment 'PRAISE/COLLECT/COMMENT/FOLLOW',
    target_id   int                                null comment '目标内容ID（文章/评论）',
    target_type varchar(20)                        null comment 'ARTICLE/COMMENT',
    is_read     tinyint  default 0                 null comment '是否已读 0-未读 1-已读',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '通知表' collate = utf8mb4_unicode_ci;

create index idx_receiver
    on notification (receiver_id, is_read);

create table review
(
    id          int auto_increment comment '评论主键'
        primary key,
    text_id     int                                not null comment '文章ID，关联到文章表',
    user_id     int                                not null comment '评论者ID',
    parent_id   int      default 0                 null comment '父评论ID，0-评论文章，other-评论用户评论',
    content     text                               not null comment '评论内容',
    create_time datetime default CURRENT_TIMESTAMP null comment '评论创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '评论表' collate = utf8mb4_unicode_ci;

create index idx_parent_id
    on review (parent_id);

create index idx_text_id
    on review (text_id);

create index idx_user_id
    on review (user_id);

create table user_foot
(
    id               int auto_increment comment '主键id'
        primary key,
    user_id          int                                not null comment '用户ID',
    document_id      int                                not null comment '文本id(文章/评论)',
    document_type    int      default 1                 null comment '文本类型：1-文章，2-评论',
    document_user_id int                                not null comment '发布该文本的用户id',
    collection_stat  int      default 0                 null comment '收藏状态 0-未收藏 1-已收藏',
    read_stat        int      default 0                 null comment '浏览状态 0-未读 1-已读',
    comment_stat     int      default 0                 null comment '评论状态 0-未评论 1-已评论',
    praise_stat      int      default 0                 null comment '点赞状态 0-未点赞 1-已点赞',
    create_time      datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_user_document
        unique (user_id, document_id, document_type)
)
    comment '用户足迹表' collate = utf8mb4_unicode_ci;

create index idx_document_user
    on user_foot (document_user_id);

create table article_stat
(
    id            int auto_increment comment '主键id'
        primary key,
    article_id    int                                not null comment '文章ID',
    view_count    bigint   default 0                 null comment '阅读数',
    praise_count  bigint   default 0                 null comment '点赞数',
    comment_count bigint   default 0                 null comment '评论数',
    collect_count bigint   default 0                 null comment '收藏数',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_article_id
        unique (article_id)
)
    comment '文章统计表' collate = utf8mb4_unicode_ci;
