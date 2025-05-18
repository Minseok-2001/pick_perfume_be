CREATE TABLE perfume_like
    (
        like_id    BIGINT AUTO_INCREMENT NOT NULL,
        perfume_id BIGINT   NOT NULL,
        member_id  BIGINT   NOT NULL,
        created_at datetime NOT NULL,
        CONSTRAINT pk_perfume_like PRIMARY KEY (like_id)
    );

ALTER TABLE perfume_like
    ADD CONSTRAINT uc_5bf932a0765dccc3fee948522 UNIQUE (perfume_id, member_id);