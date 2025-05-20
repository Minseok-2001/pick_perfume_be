CREATE TABLE survey_response_perfume_rating
    (
        rating_id    BIGINT AUTO_INCREMENT NOT NULL,
        response_id  BIGINT       NOT NULL,
        perfume_id   BIGINT       NULL,
        perfume_name VARCHAR(255) NOT NULL,
        rating       FLOAT        NOT NULL,
        is_custom    BIT(1)       NOT NULL,
        CONSTRAINT pk_survey_response_perfume_rating PRIMARY KEY (rating_id)
    );

ALTER TABLE survey_response_perfume_rating
    ADD CONSTRAINT FK_SURVEY_RESPONSE_PERFUME_RATING_ON_PERFUME FOREIGN KEY (perfume_id) REFERENCES perfume (id);