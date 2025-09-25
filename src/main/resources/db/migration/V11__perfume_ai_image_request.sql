CREATE TABLE perfume_ai_image_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perfume_id BIGINT NOT NULL,
    member_id BIGINT NULL,
    ip_address VARCHAR(64) NULL,
    status VARCHAR(30) NOT NULL,
    message VARCHAR(1000) NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_perfume_ai_image_request_perfume FOREIGN KEY (perfume_id) REFERENCES perfume (id),
    CONSTRAINT fk_perfume_ai_image_request_member FOREIGN KEY (member_id) REFERENCES member (id)
);
