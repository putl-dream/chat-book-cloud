-- Create Database
CREATE DATABASE IF NOT EXISTS chat_book_cloud;
USE chat_book_cloud;

-- Table: article
CREATE TABLE IF NOT EXISTS article (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    user_name VARCHAR(255),
    title VARCHAR(255),
    cover VARCHAR(255),
    category INT,
    abstract_text TEXT,
    status INT,
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: article_info
CREATE TABLE IF NOT EXISTS article_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    article_id INT,
    user_name VARCHAR(255),
    title VARCHAR(255),
    content LONGTEXT,
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: user
CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255),
    password VARCHAR(255),
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: user_info
CREATE TABLE IF NOT EXISTS user_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    username VARCHAR(255),
    photo VARCHAR(255),
    profile VARCHAR(255),
    role INT,
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: user_foot
CREATE TABLE IF NOT EXISTS user_foot (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    document_id INT,
    document_type INT,
    document_user_id INT,
    collection_stat INT,
    read_stat INT,
    comment_stat INT,
    praise_stat INT,
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: article_stat
CREATE TABLE IF NOT EXISTS article_stat (
    id INT AUTO_INCREMENT PRIMARY KEY,
    article_id INT NOT NULL,
    view_count BIGINT DEFAULT 0,
    praise_count BIGINT DEFAULT 0,
    comment_count BIGINT DEFAULT 0,
    collect_count BIGINT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: user_friends
CREATE TABLE IF NOT EXISTS user_friends (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id1 INT,
    user_id2 INT,
    status INT,
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: review
CREATE TABLE IF NOT EXISTS review (
    id INT AUTO_INCREMENT PRIMARY KEY,
    text_id INT,
    user_id INT,
    parent_id INT,
    content TEXT,
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: message
CREATE TABLE IF NOT EXISTS message (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT,
    receiver_id INT,
    content TEXT,
    sent_time DATETIME,
    status INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
