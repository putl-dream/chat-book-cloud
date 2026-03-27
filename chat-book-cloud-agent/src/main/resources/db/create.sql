create database chat_book_agent;

use chat_book_agent;

create table agent_session
(
    id                int auto_increment primary key,
    user_id           int                                    not null comment '用户ID',
    scene_type        varchar(16)                            not null comment 'CREATE/OPTIMIZE',
    target_article_id int                                    null comment '关联文章ID',
    target_draft_id   int                                    null comment '关联草稿ID',
    title             varchar(255) default ''                not null comment '会话标题',
    status            varchar(16) default 'ACTIVE'           not null comment 'ACTIVE/FINISHED',
    notebook_summary  longtext                               null comment '会话摘要JSON',
    model             varchar(64) default ''                 not null comment '模型名',
    prompt_version    varchar(64) default 'v1'               not null comment 'Prompt版本',
    create_time       datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time       datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_user_id (user_id),
    key idx_target_draft_id (target_draft_id)
)
    comment '文章 Agent 会话表' row_format = DYNAMIC;

create table agent_message
(
    id           int auto_increment primary key,
    session_id   int                                    not null comment '会话ID',
    role         varchar(16)                            not null comment 'USER/ASSISTANT/SYSTEM',
    content      longtext                               not null comment '消息内容',
    token_input  int          default 0                 null comment '输入token',
    token_output int          default 0                 null comment '输出token',
    latency_ms   int          default 0                 null comment '耗时毫秒',
    create_time  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    key idx_session_id (session_id)
)
    comment '文章 Agent 消息表' row_format = DYNAMIC;
