CREATE TABLE perfume_view
    (
        view_id    BIGINT AUTO_INCREMENT NOT NULL,
        perfume_id BIGINT      NOT NULL,
        member_id  BIGINT      NULL,
        view_date  date        NOT NULL,
        ip_address VARCHAR(45) NULL,
        created_at datetime    NOT NULL,
        CONSTRAINT pk_perfume_view PRIMARY KEY (view_id)
    );

CREATE TABLE post_view
    (
        view_id    BIGINT AUTO_INCREMENT NOT NULL,
        post_id    BIGINT      NOT NULL,
        member_id  BIGINT      NULL,
        view_date  date        NOT NULL,
        ip_address VARCHAR(45) NULL,
        created_at datetime    NOT NULL,
        CONSTRAINT pk_post_view PRIMARY KEY (view_id)
    );

ALTER TABLE post_view
    ADD CONSTRAINT uc_56d5b0ce48ba3fab3b4619ebc UNIQUE (post_id, member_id, view_date);

ALTER TABLE perfume_view
    ADD CONSTRAINT uc_7213f5e55557e531044dfd322 UNIQUE (perfume_id, member_id, view_date);

ALTER TABLE post
    DROP COLUMN view_count;