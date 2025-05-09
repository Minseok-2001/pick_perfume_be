CREATE TABLE SPRING_SESSION
    (
        PRIMARY_ID            CHAR(36) NOT NULL,
        SESSION_ID            CHAR(36) NOT NULL,
        CREATION_TIME         BIGINT   NOT NULL,
        LAST_ACCESS_TIME      BIGINT   NOT NULL,
        MAX_INACTIVE_INTERVAL INT      NOT NULL,
        EXPIRY_TIME           BIGINT   NOT NULL,
        PRINCIPAL_NAME        VARCHAR(100),
        CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
    )
    ENGINE = InnoDB
    ROW_FORMAT = DYNAMIC;

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES
    (
        SESSION_PRIMARY_ID CHAR(36)     NOT NULL,
        ATTRIBUTE_NAME     VARCHAR(200) NOT NULL,
        ATTRIBUTE_BYTES    BLOB         NOT NULL,
        CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
        CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION (PRIMARY_ID) ON DELETE CASCADE
    )
    ENGINE = InnoDB
    ROW_FORMAT = DYNAMIC;


CREATE TABLE accord
    (
        id            BIGINT AUTO_INCREMENT NOT NULL,
        created_at    datetime              NOT NULL,
        updated_at    datetime              NOT NULL,
        name          VARCHAR(255)          NOT NULL,
        `description` VARCHAR(255)          NULL,
        color         VARCHAR(255)          NULL,
        CONSTRAINT pk_accord PRIMARY KEY (id)
    );

CREATE TABLE brand
    (
        id            BIGINT AUTO_INCREMENT NOT NULL,
        created_at    datetime              NOT NULL,
        updated_at    datetime              NOT NULL,
        name          VARCHAR(255)          NOT NULL,
        `description` VARCHAR(255)          NULL,
        founded_year  INT                   NULL,
        website       VARCHAR(255)          NULL,
        designer      VARCHAR(255)          NULL,
        is_luxury     BIT(1)                NOT NULL,
        is_niche      BIT(1)                NOT NULL,
        is_popular    BIT(1)                NOT NULL,
        url           VARCHAR(255)          NULL,
        country_code  VARCHAR(2)            NULL,
        country_name  VARCHAR(255)          NULL,
        CONSTRAINT pk_brand PRIMARY KEY (id)
    );

CREATE TABLE designer
    (
        id                  BIGINT AUTO_INCREMENT NOT NULL,
        created_at          datetime              NOT NULL,
        updated_at          datetime              NOT NULL,
        name                VARCHAR(255)          NOT NULL,
        biography           VARCHAR(5000)         NULL,
        birth_date          date                  NULL,
        website             VARCHAR(255)          NULL,
        social_media_handle VARCHAR(255)          NULL,
        country_code        VARCHAR(2)            NULL,
        country_name        VARCHAR(255)          NULL,
        url                 VARCHAR(255)          NULL,
        CONSTRAINT pk_designer PRIMARY KEY (id)
    );

CREATE TABLE member
    (
        id          BIGINT AUTO_INCREMENT NOT NULL,
        email       VARCHAR(255)          NOT NULL,
        password    VARCHAR(255)          NOT NULL,
        nickname    VARCHAR(255)          NOT NULL,
        `role`      VARCHAR(255)          NOT NULL,
        provider    VARCHAR(255)          NOT NULL,
        provider_id VARCHAR(255)          NULL,
        created_at  datetime              NOT NULL,
        updated_at  datetime              NOT NULL,
        url         VARCHAR(255)          NULL,
        CONSTRAINT pk_member PRIMARY KEY (id)
    );

CREATE TABLE member_activity
    (
        id                  BIGINT AUTO_INCREMENT NOT NULL,
        member_id           BIGINT                NOT NULL,
        activity_type       SMALLINT              NOT NULL,
        perfume_id          BIGINT                NOT NULL,
        rating              INT                   NULL,
        vote_category       VARCHAR(255)          NULL,
        vote_value          VARCHAR(255)          NULL,
        recommendation_type VARCHAR(255)          NULL,
        timestamp           datetime              NOT NULL,
        CONSTRAINT pk_member_activity PRIMARY KEY (id)
    );

CREATE TABLE member_preference
    (
        member_id            BIGINT   NOT NULL,
        created_at           datetime NOT NULL,
        updated_at           datetime NOT NULL,
        preferred_notes      JSON     NULL,
        preferred_accords    JSON     NULL,
        preferred_brands     JSON     NULL,
        reviewed_perfume_ids JSON     NULL,
        last_updated         datetime NOT NULL,
        CONSTRAINT pk_member_preference PRIMARY KEY (member_id)
    );

CREATE TABLE note
    (
        id            BIGINT AUTO_INCREMENT NOT NULL,
        created_at    datetime              NOT NULL,
        updated_at    datetime              NOT NULL,
        name          VARCHAR(255)          NOT NULL,
        `description` VARCHAR(255)          NULL,
        url           VARCHAR(255)          NULL,
        CONSTRAINT pk_note PRIMARY KEY (id)
    );

CREATE TABLE perfume
    (
        id            BIGINT AUTO_INCREMENT NOT NULL,
        created_at    datetime              NOT NULL,
        updated_at    datetime              NOT NULL,
        name          VARCHAR(255)          NOT NULL,
        `description` VARCHAR(5000)         NULL,
        release_year  INT                   NULL,
        brand_id      BIGINT                NOT NULL,
        concentration VARCHAR(255)          NULL,
        is_approved   BIT(1)                NOT NULL,
        creator_id    BIGINT                NULL,
        url           VARCHAR(255)          NULL,
        CONSTRAINT pk_perfume PRIMARY KEY (id)
    );

CREATE TABLE perfume_accord
    (
        id         BIGINT AUTO_INCREMENT NOT NULL,
        created_at datetime              NOT NULL,
        updated_at datetime              NOT NULL,
        perfume_id BIGINT                NOT NULL,
        accord_id  BIGINT                NOT NULL,
        CONSTRAINT pk_perfume_accord PRIMARY KEY (id)
    );

CREATE TABLE perfume_designer
    (
        id            BIGINT AUTO_INCREMENT NOT NULL,
        created_at    datetime              NOT NULL,
        updated_at    datetime              NOT NULL,
        perfume_id    BIGINT                NOT NULL,
        designer_id   BIGINT                NOT NULL,
        `role`        VARCHAR(255)          NOT NULL,
        `description` VARCHAR(255)          NULL,
        CONSTRAINT pk_perfume_designer PRIMARY KEY (id)
    );

CREATE TABLE perfume_note
    (
        id         BIGINT AUTO_INCREMENT NOT NULL,
        created_at datetime              NOT NULL,
        updated_at datetime              NOT NULL,
        perfume_id BIGINT                NOT NULL,
        note_id    BIGINT                NOT NULL,
        type       VARCHAR(255)          NOT NULL,
        CONSTRAINT pk_perfume_note PRIMARY KEY (id)
    );

CREATE TABLE perfume_vote_statistics
    (
        perfume_id   BIGINT   NOT NULL,
        created_at   datetime NOT NULL,
        updated_at   datetime NOT NULL,
        statistics   JSON     NULL,
        last_updated datetime NOT NULL,
        CONSTRAINT pk_perfume_vote_statistics PRIMARY KEY (perfume_id)
    );

CREATE TABLE review
    (
        id          BIGINT AUTO_INCREMENT NOT NULL,
        created_at  datetime              NOT NULL,
        updated_at  datetime              NOT NULL,
        member_id   BIGINT                NOT NULL,
        perfume_id  BIGINT                NOT NULL,
        content     VARCHAR(10000)        NOT NULL,
        season      VARCHAR(255)          NULL,
        time_of_day VARCHAR(255)          NULL,
        sentiment   VARCHAR(255)          NULL,
        value       INT                   NOT NULL,
        CONSTRAINT pk_review PRIMARY KEY (id)
    );

CREATE TABLE review_reaction
    (
        id         BIGINT AUTO_INCREMENT NOT NULL,
        created_at datetime              NOT NULL,
        updated_at datetime              NOT NULL,
        member_id  BIGINT                NOT NULL,
        review_id  BIGINT                NOT NULL,
        is_like    BIT(1)                NOT NULL,
        CONSTRAINT pk_review_reaction PRIMARY KEY (id)
    );

CREATE TABLE vote
    (
        id         BIGINT AUTO_INCREMENT NOT NULL,
        created_at datetime              NOT NULL,
        updated_at datetime              NOT NULL,
        member_id  BIGINT                NOT NULL,
        perfume_id BIGINT                NOT NULL,
        category   VARCHAR(255)          NOT NULL,
        value      VARCHAR(255)          NOT NULL,
        CONSTRAINT pk_vote PRIMARY KEY (id)
    );

ALTER TABLE perfume_designer
    ADD CONSTRAINT uc_7e648122fd2cfde01621668e6 UNIQUE (perfume_id, designer_id, `role`);

ALTER TABLE accord
    ADD CONSTRAINT uc_accord_name UNIQUE (name);

ALTER TABLE brand
    ADD CONSTRAINT uc_brand_name UNIQUE (name);

ALTER TABLE review_reaction
    ADD CONSTRAINT uc_df95212f4739d077e931c4b53 UNIQUE (member_id, review_id);

ALTER TABLE vote
    ADD CONSTRAINT uc_e475cbff9803d6dc3668cea9b UNIQUE (member_id, perfume_id, category);

ALTER TABLE member
    ADD CONSTRAINT uc_member_email UNIQUE (email);

ALTER TABLE note
    ADD CONSTRAINT uc_note_name UNIQUE (name);