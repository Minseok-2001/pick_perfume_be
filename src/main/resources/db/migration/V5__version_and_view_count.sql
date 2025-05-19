ALTER TABLE perfume
    ADD version BIGINT DEFAULT 0 NULL;

ALTER TABLE perfume
    ADD view_count INT DEFAULT 0 NULL;

ALTER TABLE perfume
    MODIFY version BIGINT NOT NULL;

ALTER TABLE perfume
    MODIFY view_count INT NOT NULL;