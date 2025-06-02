CREATE TABLE survey_recommendation
    (
        id                   BIGINT AUTO_INCREMENT NOT NULL,
        survey_id            BIGINT   NOT NULL,
        member_id            BIGINT   NULL,
        perfume_id           BIGINT   NOT NULL,
        recommendation_score FLOAT    NOT NULL,
        recommendation_rank  INT      NOT NULL,
        created_at           datetime NOT NULL,
        CONSTRAINT pk_survey_recommendation PRIMARY KEY (id)
    );

CREATE TABLE survey_recommendation_feedback
    (
        id                       BIGINT AUTO_INCREMENT NOT NULL,
        survey_recommendation_id BIGINT        NOT NULL,
        member_id                BIGINT        NULL,
        feedback_type            VARCHAR(255)  NOT NULL,
        rating                   INT           NULL,
        comment                  VARCHAR(1000) NULL,
        created_at               datetime      NOT NULL,
        CONSTRAINT pk_survey_recommendation_feedback PRIMARY KEY (id)
    );

ALTER TABLE survey_recommendation_feedback
    ADD CONSTRAINT FK_SURVEY_RECOMMENDATION_FEEDBACK_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE survey_recommendation_feedback
    ADD CONSTRAINT FK_SURVEY_RECOMMENDATION_FEEDBACK_ON_SURVEY_RECOMMENDATION FOREIGN KEY (survey_recommendation_id) REFERENCES survey_recommendation (id);

ALTER TABLE survey_recommendation
    ADD CONSTRAINT FK_SURVEY_RECOMMENDATION_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE survey_recommendation
    ADD CONSTRAINT FK_SURVEY_RECOMMENDATION_ON_PERFUME FOREIGN KEY (perfume_id) REFERENCES perfume (id);

ALTER TABLE survey_recommendation
    ADD CONSTRAINT FK_SURVEY_RECOMMENDATION_ON_SURVEY FOREIGN KEY (survey_id) REFERENCES survey (survey_id);