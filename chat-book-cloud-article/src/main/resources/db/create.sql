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

create table author_tag
(
    id              int auto_increment primary key,
    name            varchar(32) collate utf8mb4_bin        not null comment '作者标签展示名称',
    normalized_name varchar(64)                            not null default '' comment '轻量规范化名称',
    creator_id      int                                    null comment '首个创建者',
    status          varchar(16)                            not null default 'ACTIVE' comment 'ACTIVE/DISABLED',
    create_time     datetime                               not null default CURRENT_TIMESTAMP,
    update_time     datetime                               not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    unique key uk_author_tag_name (name),
    key idx_author_tag_normalized_name (normalized_name),
    key idx_author_tag_status (status)
) comment '作者标签表' row_format = DYNAMIC;

create table article_author_tag_rel
(
    id             int auto_increment primary key,
    article_id     int                                    not null comment '文章ID',
    author_tag_id  int                                    not null comment '作者标签ID',
    create_time    datetime                               not null default CURRENT_TIMESTAMP,
    unique key uk_article_author_tag (article_id, author_tag_id),
    key idx_article_author_tag_rel_tag_id (author_tag_id),
    constraint fk_article_author_tag_rel_article foreign key (article_id) references article(id) on delete cascade,
    constraint fk_article_author_tag_rel_tag foreign key (author_tag_id) references author_tag(id) on delete cascade
) comment '文章作者标签关联表' row_format = DYNAMIC;

create table system_tag
(
    id                int auto_increment primary key,
    name              varchar(32)                            not null comment '系统标签名称',
    code              varchar(64)                            not null comment '稳定编码',
    dimension         varchar(32)                            not null comment '标签维度',
    description       varchar(255)                           not null default '' comment '标签描述',
    status            varchar(16)                            not null default 'ACTIVE' comment 'ACTIVE/DISABLED',
    sort              int                                    not null default 0 comment '排序权重',
    recommend_weight  decimal(10, 2)                         not null default 1.00 comment '推荐权重',
    create_time       datetime                               not null default CURRENT_TIMESTAMP,
    update_time       datetime                               not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    unique key uk_system_tag_code (code),
    key idx_system_tag_dimension_status (dimension, status)
) comment '系统标签表' row_format = DYNAMIC;

create table article_system_tag_rel
(
    id             int auto_increment primary key,
    article_id     int                                    not null comment '文章ID',
    system_tag_id  int                                    not null comment '系统标签ID',
    source         varchar(16)                            not null default 'RULE' comment 'RULE/VECTOR/ADMIN',
    confidence     decimal(10, 2)                         not null default 1.00 comment '置信度',
    create_time    datetime                               not null default CURRENT_TIMESTAMP,
    update_time    datetime                               not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    unique key uk_article_system_tag (article_id, system_tag_id),
    key idx_article_system_tag_rel_tag_id (system_tag_id),
    key idx_article_system_tag_rel_article_source (article_id, source),
    constraint fk_article_system_tag_rel_article foreign key (article_id) references article(id) on delete cascade,
    constraint fk_article_system_tag_rel_tag foreign key (system_tag_id) references system_tag(id) on delete cascade
) comment '文章系统标签关联表' row_format = DYNAMIC;

create table author_tag_system_tag_map
(
    id             int auto_increment primary key,
    author_tag_id  int                                    not null comment '作者标签ID',
    system_tag_id  int                                    not null comment '系统标签ID',
    source         varchar(16)                            not null default 'RULE' comment 'RULE/VECTOR/ADMIN',
    confidence     decimal(10, 2)                         not null default 1.00 comment '置信度',
    status         varchar(16)                            not null default 'ACTIVE' comment 'ACTIVE/DISABLED',
    create_time    datetime                               not null default CURRENT_TIMESTAMP,
    update_time    datetime                               not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    unique key uk_author_system_tag_map (author_tag_id, system_tag_id),
    key idx_author_system_tag_map_tag (system_tag_id),
    key idx_author_system_tag_map_status (status),
    constraint fk_author_system_tag_map_author foreign key (author_tag_id) references author_tag(id) on delete cascade,
    constraint fk_author_system_tag_map_system foreign key (system_tag_id) references system_tag(id) on delete cascade
) comment '作者标签与系统标签映射表' row_format = DYNAMIC;

insert into system_tag (name, code, dimension, description, sort, recommend_weight) values
('AI / 大模型', 'TOPIC_AI_LLM', 'topic', 'AI 与大模型相关内容', 10, 1.20),
('技术选型', 'TOPIC_TECH_SELECTION', 'topic', '技术选型与方案比较', 20, 1.00),
('架构设计', 'TOPIC_ARCHITECTURE', 'topic', '系统架构与设计方法', 30, 1.10),
('工程实践', 'TOPIC_ENGINEERING', 'topic', '工程化与交付实践', 40, 1.05),
('产品思考', 'TOPIC_PRODUCT', 'intent', '面向产品与业务的思考', 50, 0.95),
('行业观察', 'TOPIC_INDUSTRY', 'scene', '行业趋势与观察', 60, 0.90),
('Java', 'STACK_JAVA', 'stack', 'Java 技术栈', 110, 1.00),
('Python', 'STACK_PYTHON', 'stack', 'Python 技术栈', 120, 1.00),
('Go', 'STACK_GO', 'stack', 'Go 技术栈', 130, 1.00),
('Vue', 'STACK_VUE', 'stack', 'Vue 技术栈', 140, 1.00),
('React', 'STACK_REACT', 'stack', 'React 技术栈', 150, 1.00),
('MySQL', 'STACK_MYSQL', 'stack', 'MySQL 技术栈', 160, 1.00),
('Spring Boot', 'STACK_SPRING_BOOT', 'stack', 'Spring Boot 技术栈', 170, 1.00),
('Docker', 'STACK_DOCKER', 'stack', 'Docker 技术栈', 180, 1.00),
('入门', 'AUDIENCE_BEGINNER', 'audience', '适合入门读者', 210, 0.90),
('进阶', 'AUDIENCE_ADVANCED', 'audience', '适合进阶读者', 220, 1.00),
('实战', 'SCENE_PRACTICE', 'scene', '偏实战落地内容', 230, 1.05),
('源码', 'SCENE_SOURCE_CODE', 'scene', '偏源码分析内容', 240, 0.95),
('面试', 'SCENE_INTERVIEW', 'scene', '偏面试准备内容', 250, 0.90);

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
