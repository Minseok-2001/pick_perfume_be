ALTER TABLE member_preference
    ADD version BIGINT NULL;

ALTER TABLE member_preference MODIFY version BIGINT NOT NULL;