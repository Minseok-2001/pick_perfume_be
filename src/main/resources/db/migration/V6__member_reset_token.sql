CREATE TABLE member_reset_token
    (
        token           VARCHAR(255) NOT NULL,
        created_at      datetime     NOT NULL,
        updated_at      datetime     NOT NULL,
        member_id       BIGINT       NULL,
        email           VARCHAR(255) NOT NULL,
        expiration_date BIGINT       NOT NULL,
        is_used         BIT(1)       NOT NULL,
        CONSTRAINT pk_member_reset_token PRIMARY KEY (token)
    );