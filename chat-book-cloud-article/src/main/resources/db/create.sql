create table article
(
    id            int auto_increment comment '主键id'
        primary key,
    user_id       int                                    not null comment '作者id',
    user_name     varchar(30)  default ''                not null,
    title         varchar(48)  default ''                not null comment '标题',
    cover         varchar(255) default ''                not null comment '封面图',
    category      tinyint      default 4                 not null comment '分类 0-后端 1-前端 2-MySQL 3-算法 4-other',
    content_type  tinyint      default 2                 not null comment '内容类型 0-学习/教程 1-实战/项目 2-未分类',
    abstract_text varchar(255) default ''                not null comment '摘要',
    status        tinyint      default 0                 not null comment '文章状态 0-草稿、1待审核、2已发布、-1删除',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null comment '最后更新时间'
)
    comment '文章表' row_format = DYNAMIC;

create table tag
(
    id          int auto_increment primary key,
    name        varchar(32)  not null comment '标签名称',
    type        tinyint      not null default 1 comment '标签类型 1-技术栈 2-学习路径',
    color       varchar(16)  default '#409EFF' comment '标签颜色',
    sort        int          default 0 comment '排序权重',
    create_time datetime     default CURRENT_TIMESTAMP,
    update_time datetime     default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    unique key uk_name_type (name, type)
) comment '标签表' row_format = DYNAMIC;

create table article_tag
(
    id          int auto_increment primary key,
    article_id  int          not null comment '文章ID',
    tag_id      int          not null comment '标签ID',
    create_time datetime     default CURRENT_TIMESTAMP,
    unique key uk_article_tag (article_id, tag_id),
    key idx_tag_id (tag_id),
    constraint fk_article_tag_article foreign key (article_id) references article(id) on delete cascade,
    constraint fk_article_tag_tag foreign key (tag_id) references tag(id) on delete cascade
) comment '文章标签关联表' row_format = DYNAMIC;

-- 初始化预置标签数据
insert into tag (name, type, color, sort) values
-- 技术栈标签 (type=1)
('Java', 1, '#B07219', 1),
('Python', 1, '#3572A5', 2),
('Go', 1, '#00ADD8', 3),
('Vue', 1, '#41B883', 4),
('React', 1, '#61DAFB', 5),
('MySQL', 1, '#4479A1', 6),
('Spring Boot', 1, '#6DB33F', 7),
('Docker', 1, '#2496ED', 8),
-- 学习路径标签 (type=2)
('入门', 2, '#67C23A', 100),
('进阶', 2, '#E6A23C', 101),
('实战', 2, '#F56C6C', 102),
('源码', 2, '#909399', 103),
('面试', 2, '#C71585', 104);

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
    constraint uk_article_info_article_id
        unique (article_id),
    constraint article_data_article_id_fk
        foreign key (article_id) references article (id)
)
    comment '文章详情表' row_format = DYNAMIC;

create table article_review_log
(
    id            bigint auto_increment primary key,
    article_id    int                                    not null comment '文章ID',
    reviewer_id   int                                    not null comment '审核人ID',
    reviewer_name varchar(48)  default ''                not null comment '审核人用户名快照',
    review_action varchar(16)                            not null comment '审核动作 APPROVE/REJECT',
    review_reason varchar(255) default ''                not null comment '审核原因/备注',
    batch_id      varchar(64)  default ''                null comment '批量审核批次号',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '审核时间',
    key idx_article_review_article_id (article_id),
    key idx_article_review_reviewer_id (reviewer_id)
)
    comment '文章审核日志表' row_format = DYNAMIC;
