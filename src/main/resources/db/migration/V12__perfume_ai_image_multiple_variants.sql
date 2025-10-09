CREATE TABLE perfume_ai_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perfume_id BIGINT NOT NULL,
    prompt_type VARCHAR(50) NOT NULL,
    prompt TEXT NOT NULL,
    image_url VARCHAR(1024) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_perfume_ai_image_perfume FOREIGN KEY (perfume_id) REFERENCES perfume (id),
    CONSTRAINT uk_perfume_ai_image_prompt UNIQUE (perfume_id, prompt_type)
);

ALTER TABLE perfume_ai_image_request
    ADD COLUMN prompt_type VARCHAR(50) NOT NULL DEFAULT 'STORYBOARD_IMPRESSION' AFTER ip_address,
    ADD COLUMN prompt TEXT NULL AFTER message;

UPDATE perfume_ai_image_request
SET prompt_type = 'STORYBOARD_IMPRESSION'
WHERE prompt_type IS NULL;

ALTER TABLE perfume_ai_image_request
    MODIFY prompt_type VARCHAR(50) NOT NULL;

CREATE TABLE perfume_ai_image_vote (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perfume_id BIGINT NOT NULL,
    perfume_ai_image_id BIGINT NOT NULL,
    member_id BIGINT NULL,
    ip_address VARCHAR(64) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_perfume_ai_image_vote_perfume FOREIGN KEY (perfume_id) REFERENCES perfume (id),
    CONSTRAINT fk_perfume_ai_image_vote_ai_image FOREIGN KEY (perfume_ai_image_id) REFERENCES perfume_ai_image (id),
    CONSTRAINT fk_perfume_ai_image_vote_member FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE UNIQUE INDEX uk_perfume_ai_image_vote_member ON perfume_ai_image_vote (perfume_id, member_id);
CREATE INDEX idx_perfume_ai_image_vote_perfume ON perfume_ai_image_vote (perfume_id);
CREATE INDEX idx_perfume_ai_image_vote_ai_image ON perfume_ai_image_vote (perfume_ai_image_id);
CREATE UNIQUE INDEX uk_perfume_ai_image_vote_ip ON perfume_ai_image_vote (perfume_id, ip_address);
