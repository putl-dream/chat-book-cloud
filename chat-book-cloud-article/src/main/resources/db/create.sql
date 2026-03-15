create table article
(
    id            int auto_increment comment '主键id'
        primary key,
    user_id       int                                    not null comment '作者id',
    user_name     varchar(30)  default ''                not null,
    title         varchar(48)  default ''                not null comment '标题',
    cover         varchar(255) default ''                not null comment '封面图',
    category      tinyint      default 4                 not null comment '分类 0-后端 1-前端 2-MySQL 3-算法 4-other',
    abstract_text varchar(255) default ''                not null comment '摘要',
    status        tinyint      default 0                 not null comment '文章状态 0-草稿、1待审核、2已发布、-1删除',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null comment '最后更新时间'
)
    comment '文章表' row_format = DYNAMIC;

create table article_info
(
    id          int auto_increment comment '主键id'
        primary key,
    user_id     int                                    not null comment '用户id',
    article_id  int                                    not null comment '文章id',
    user_name   varchar(48)  default ''                not null comment '作者昵称',
    title       varchar(255) default ''                not null comment '文章标题',
    content     text                                   not null comment '内容',
    create_time datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime     default CURRENT_TIMESTAMP not null,
    constraint article_data_article_id_fk
        foreign key (article_id) references article (id)
)
    comment '文章详情表' row_format = DYNAMIC;
