CREATE TABLE board
    (
        board_id      BIGINT AUTO_INCREMENT NOT NULL,
        name          VARCHAR(255)          NOT NULL,
        display_name  VARCHAR(255)          NOT NULL,
        content       TEXT                  NULL,
        is_active     BIT(1)                NOT NULL,
        display_order INT                   NOT NULL,
        created_at    datetime              NOT NULL,
        updated_at    datetime              NOT NULL,
        CONSTRAINT pk_board PRIMARY KEY (board_id)
    );

CREATE TABLE comment
    (
        comment_id BIGINT AUTO_INCREMENT NOT NULL,
        content    TEXT                  NOT NULL,
        post_id    BIGINT                NOT NULL,
        member_id  BIGINT                NOT NULL,
        parent_id  BIGINT                NULL,
        created_at datetime              NOT NULL,
        updated_at datetime              NOT NULL,
        created_by VARCHAR(255)          NULL,
        updated_by VARCHAR(255)          NULL,
        is_deleted BIT(1)                NOT NULL,
        CONSTRAINT pk_comment PRIMARY KEY (comment_id)
    );

CREATE TABLE comment_like
    (
        like_id    BIGINT AUTO_INCREMENT NOT NULL,
        comment_id BIGINT                NOT NULL,
        member_id  BIGINT                NOT NULL,
        created_at datetime              NOT NULL,
        CONSTRAINT pk_comment_like PRIMARY KEY (like_id)
    );

CREATE TABLE post
    (
        post_id    BIGINT AUTO_INCREMENT NOT NULL,
        title      VARCHAR(255)          NOT NULL,
        content    TEXT                  NOT NULL,
        member_id  BIGINT                NOT NULL,
        board_id   BIGINT                NOT NULL,
        view_count BIGINT                NOT NULL,
        created_at datetime              NOT NULL,
        updated_at datetime              NOT NULL,
        created_by VARCHAR(255)          NULL,
        updated_by VARCHAR(255)          NULL,
        is_deleted BIT(1)                NOT NULL,
        CONSTRAINT pk_post PRIMARY KEY (post_id)
    );

CREATE TABLE post_like
    (
        like_id    BIGINT AUTO_INCREMENT NOT NULL,
        post_id    BIGINT                NOT NULL,
        member_id  BIGINT                NOT NULL,
        created_at datetime              NOT NULL,
        CONSTRAINT pk_post_like PRIMARY KEY (like_id)
    );

CREATE TABLE post_perfume_embed
    (
        embed_id   BIGINT AUTO_INCREMENT NOT NULL,
        post_id    BIGINT                NOT NULL,
        perfume_id BIGINT                NOT NULL,
        created_at datetime              NOT NULL,
        CONSTRAINT pk_post_perfume_embed PRIMARY KEY (embed_id)
    );

ALTER TABLE post_perfume_embed
    ADD CONSTRAINT uc_0370f62cf23f6ee5309f9722c UNIQUE (post_id, perfume_id);

ALTER TABLE comment_like
    ADD CONSTRAINT uc_4cac1be3f14fbe3a7dc2fe076 UNIQUE (comment_id, member_id);

ALTER TABLE post_like
    ADD CONSTRAINT uc_9320da6d4cf518acdca89400c UNIQUE (post_id, member_id);

ALTER TABLE board
    ADD CONSTRAINT uc_board_name UNIQUE (name);