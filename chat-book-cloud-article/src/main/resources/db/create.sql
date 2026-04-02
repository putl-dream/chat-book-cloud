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
    article_type  varchar(16)  default ''                not null comment '文章类型 ORIGINAL/REPRINT/TRANSLATION',
    creation_statement varchar(128) default ''           not null comment '创作声明，逗号分隔 PERSONAL_VIEW/NETWORK_SOURCE/AI_ASSISTED',
    status        tinyint      default 0                 not null comment '文章状态 0-草稿、1待审核、2已发布、-1删除',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null comment '最后更新时间'
)
    comment '文章表' row_format = DYNAMIC;

create table tag
(
    id          int auto_increment primary key,
    name        varchar(32)  not null comment '标签名称',
    type        tinyint      not null default 1 comment '标签类型 1-技术栈 2-学习路径 3-主题标签',
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
-- 主题标签 (type=3)
('AI / 大模型', 3, '#7C3AED', 1),
('技术选型', 3, '#0F766E', 2),
('架构设计', 3, '#EA580C', 3),
('工程实践', 3, '#2563EB', 4),
('产品思考', 3, '#BE185D', 5),
('行业观察', 3, '#475569', 6),
-- 技术栈标签 (type=1)
('Java', 1, '#B07219', 101),
('Python', 1, '#3572A5', 102),
('Go', 1, '#00ADD8', 103),
('Vue', 1, '#41B883', 104),
('React', 1, '#61DAFB', 105),
('MySQL', 1, '#4479A1', 106),
('Spring Boot', 1, '#6DB33F', 107),
('Docker', 1, '#2496ED', 108),
-- 学习路径标签 (type=2)
('入门', 2, '#67C23A', 201),
('进阶', 2, '#E6A23C', 202),
('实战', 2, '#F56C6C', 203),
('源码', 2, '#909399', 204),
('面试', 2, '#C71585', 205);

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

create table article_draft
(
    id                 int auto_increment primary key,
    user_id            int                                    not null comment '用户ID',
    source_session_id  int                                    null comment '来源会话ID',
    title              varchar(255) default ''                not null comment '当前标题',
    summary            text                                   null comment '当前摘要',
    content            longtext                               null comment '当前正文',
    current_version_no int          default 1                 not null comment '当前版本号',
    status             varchar(16)  default 'DRAFT'           not null comment 'DRAFT/PUBLISHED/ABANDONED',
    create_time        datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time        datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_article_draft_user_id (user_id),
    key idx_article_draft_source_session_id (source_session_id)
)
    comment '文章草稿表' row_format = DYNAMIC;

create table article_draft_version
(
    id          int auto_increment primary key,
    draft_id    int                                    not null comment '草稿ID',
    version_no  int                                    not null comment '版本号',
    source_type varchar(16)                            not null comment 'CREATE/OPTIMIZE/USER_EDIT',
    instruction text                                   null comment '生成或优化指令',
    title       varchar(255) default ''                not null comment '标题',
    summary     text                                   null comment '摘要',
    content     longtext                               null comment '正文',
    adopted     tinyint      default 0                 not null comment '是否采用',
    create_time datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    unique key uk_article_draft_version (draft_id, version_no),
    key idx_article_draft_version_draft_id (draft_id)
)
    comment '文章草稿版本表' row_format = DYNAMIC;
