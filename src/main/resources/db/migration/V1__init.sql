CREATE TABLE accord
    (
        id         BIGINT AUTO_INCREMENT NOT NULL,
        created_at datetime     NOT NULL,
        updated_at datetime     NOT NULL,
        name       VARCHAR(255) NOT NULL,
        content    VARCHAR(255) NULL,
        color      VARCHAR(255) NULL,
        CONSTRAINT pk_accord PRIMARY KEY (id)
    );

CREATE TABLE brand
    (
        id           BIGINT AUTO_INCREMENT NOT NULL,
        created_at   datetime     NOT NULL,
        updated_at   datetime     NOT NULL,
        name         VARCHAR(255) NOT NULL,
        content      VARCHAR(255) NULL,
        founded_year INT          NULL,
        website      VARCHAR(255) NULL,
        designer     VARCHAR(255) NULL,
        is_luxury    BIT(1)       NOT NULL,
        is_niche     BIT(1)       NOT NULL,
        is_popular   BIT(1)       NOT NULL,
        url          VARCHAR(255) NULL,
        country_code VARCHAR(2)   NULL,
        country_name VARCHAR(255) NULL,
        CONSTRAINT pk_brand PRIMARY KEY (id)
    );

CREATE TABLE designer
    (
        id                  BIGINT AUTO_INCREMENT NOT NULL,
        created_at          datetime      NOT NULL,
        updated_at          datetime      NOT NULL,
        name                VARCHAR(255)  NOT NULL,
        biography           VARCHAR(5000) NULL,
        birth_date          date          NULL,
        website             VARCHAR(255)  NULL,
        social_media_handle VARCHAR(255)  NULL,
        country_code        VARCHAR(2)    NULL,
        country_name        VARCHAR(255)  NULL,
        url                 VARCHAR(255)  NULL,
        CONSTRAINT pk_designer PRIMARY KEY (id)
    );

CREATE TABLE member
    (
        id           BIGINT AUTO_INCREMENT NOT NULL,
        created_at   datetime     NOT NULL,
        updated_at   datetime     NOT NULL,
        email        VARCHAR(255) NOT NULL,
        password     VARCHAR(255) NULL,
        nickname     VARCHAR(255) NOT NULL,
        name         VARCHAR(255) NULL,
        member_role  VARCHAR(255) NOT NULL,
        provider     VARCHAR(255) NOT NULL,
        provider_id  VARCHAR(255) NULL,
        phone_number VARCHAR(255) NULL,
        url          VARCHAR(255) NULL,
        CONSTRAINT pk_member PRIMARY KEY (id)
    );

CREATE TABLE member_activity
    (
        id                  BIGINT AUTO_INCREMENT NOT NULL,
        member_id           BIGINT       NOT NULL,
        activity_type       SMALLINT     NOT NULL,
        perfume_id          BIGINT       NOT NULL,
        rating              INT          NULL,
        vote_category       VARCHAR(255) NULL,
        vote_value          VARCHAR(255) NULL,
        recommendation_type VARCHAR(255) NULL,
        timestamp           datetime     NOT NULL,
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
        version              BIGINT   NOT NULL,
        CONSTRAINT pk_member_preference PRIMARY KEY (member_id)
    );

CREATE TABLE note
    (
        id         BIGINT AUTO_INCREMENT NOT NULL,
        created_at datetime     NOT NULL,
        updated_at datetime     NOT NULL,
        name       VARCHAR(255) NOT NULL,
        content    VARCHAR(255) NULL,
        url        VARCHAR(255) NULL,
        CONSTRAINT pk_note PRIMARY KEY (id)
    );

CREATE TABLE perfume
    (
        id            BIGINT AUTO_INCREMENT NOT NULL,
        created_at    datetime         NOT NULL,
        updated_at    datetime         NOT NULL,
        name          VARCHAR(255)     NOT NULL,
        content       VARCHAR(5000)    NULL,
        release_year  INT              NULL,
        gender        VARCHAR(255)     NULL,
        brand_id      BIGINT           NOT NULL,
        concentration VARCHAR(255)     NULL,
        is_approved   BIT(1) DEFAULT 0 NOT NULL,
        creator_id    BIGINT           NULL,
        search_synced BIT(1)           NOT NULL,
        url           VARCHAR(255)     NULL,
        CONSTRAINT pk_perfume PRIMARY KEY (id)
    );

CREATE TABLE perfume_accord
    (
        id         BIGINT AUTO_INCREMENT NOT NULL,
        created_at datetime NOT NULL,
        updated_at datetime NOT NULL,
        perfume_id BIGINT   NOT NULL,
        accord_id  BIGINT   NOT NULL,
        position   INT      NULL,
        CONSTRAINT pk_perfume_accord PRIMARY KEY (id)
    );

CREATE TABLE perfume_designer
    (
        id            BIGINT AUTO_INCREMENT NOT NULL,
        created_at    datetime     NOT NULL,
        updated_at    datetime     NOT NULL,
        perfume_id    BIGINT       NOT NULL,
        designer_id   BIGINT       NOT NULL,
        designer_role VARCHAR(255) NOT NULL,
        content       VARCHAR(500) NULL,
        CONSTRAINT pk_perfume_designer PRIMARY KEY (id)
    );

CREATE TABLE perfume_note
    (
        id         BIGINT AUTO_INCREMENT NOT NULL,
        created_at datetime     NOT NULL,
        updated_at datetime     NOT NULL,
        perfume_id BIGINT       NOT NULL,
        note_id    BIGINT       NOT NULL,
        type       VARCHAR(255) NOT NULL,
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
        created_at  datetime       NOT NULL,
        updated_at  datetime       NOT NULL,
        member_id   BIGINT         NOT NULL,
        perfume_id  BIGINT         NOT NULL,
        content     VARCHAR(10000) NOT NULL,
        season      VARCHAR(255)   NULL,
        time_of_day VARCHAR(255)   NULL,
        sentiment   VARCHAR(255)   NULL,
        value       INT            NOT NULL,
        CONSTRAINT pk_review PRIMARY KEY (id)
    );

CREATE TABLE review_reaction
    (
        id         BIGINT AUTO_INCREMENT NOT NULL,
        created_at datetime NOT NULL,
        updated_at datetime NOT NULL,
        member_id  BIGINT   NOT NULL,
        review_id  BIGINT   NOT NULL,
        is_like    BIT(1)   NOT NULL,
        CONSTRAINT pk_review_reaction PRIMARY KEY (id)
    );

CREATE TABLE survey
    (
        survey_id  BIGINT AUTO_INCREMENT NOT NULL,
        created_at datetime     NOT NULL,
        updated_at datetime     NOT NULL,
        member_id  BIGINT       NULL,
        image_url  VARCHAR(255) NULL,
        status     VARCHAR(255) NOT NULL,
        CONSTRAINT pk_survey PRIMARY KEY (survey_id)
    );

CREATE TABLE survey_response
    (
        response_id   BIGINT AUTO_INCREMENT NOT NULL,
        created_at    datetime NOT NULL,
        updated_at    datetime NOT NULL,
        survey_id     BIGINT   NOT NULL,
        question_id   BIGINT   NOT NULL,
        slider_answer INT      NULL,
        CONSTRAINT pk_survey_response PRIMARY KEY (response_id)
    );

CREATE TABLE survey_response_choice
    (
        choice_id   BIGINT AUTO_INCREMENT NOT NULL,
        response_id BIGINT       NOT NULL,
        option_text VARCHAR(255) NOT NULL,
        CONSTRAINT pk_survey_response_choice PRIMARY KEY (choice_id)
    );

CREATE TABLE survey_response_matrix
    (
        matrix_id   BIGINT AUTO_INCREMENT NOT NULL,
        response_id BIGINT       NOT NULL,
        option_key  VARCHAR(255) NOT NULL,
        value       INT          NOT NULL,
        CONSTRAINT pk_survey_response_matrix PRIMARY KEY (matrix_id)
    );

CREATE TABLE survey_template
    (
        question_id    BIGINT AUTO_INCREMENT NOT NULL,
        question_key   VARCHAR(255) NOT NULL,
        question_text  TEXT         NOT NULL,
        question_type  VARCHAR(255) NOT NULL,
        max_selections INT          NULL,
        required       BIT(1)       NOT NULL,
        sort_order     INT          NOT NULL,
        CONSTRAINT pk_survey_template PRIMARY KEY (question_id)
    );

CREATE TABLE survey_template_option
    (
        option_id   BIGINT AUTO_INCREMENT NOT NULL,
        question_id BIGINT NOT NULL,
        option_text TEXT   NOT NULL,
        sort_order  INT    NOT NULL,
        CONSTRAINT pk_survey_template_option PRIMARY KEY (option_id)
    );

CREATE TABLE survey_template_scale
    (
        scale_id    BIGINT AUTO_INCREMENT NOT NULL,
        question_id BIGINT NOT NULL,
        min_value   INT    NOT NULL,
        max_value   INT    NOT NULL,
        step_value  DOUBLE NULL,
        labels      TEXT   NULL,
        CONSTRAINT pk_survey_template_scale PRIMARY KEY (scale_id)
    );

CREATE TABLE vote
    (
        id         BIGINT AUTO_INCREMENT NOT NULL,
        created_at datetime     NOT NULL,
        updated_at datetime     NOT NULL,
        member_id  BIGINT       NOT NULL,
        perfume_id BIGINT       NOT NULL,
        category   VARCHAR(255) NOT NULL,
        value      VARCHAR(255) NOT NULL,
        CONSTRAINT pk_vote PRIMARY KEY (id)
    );

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

ALTER TABLE survey_template
    ADD CONSTRAINT uc_survey_template_question_key UNIQUE (question_key);

ALTER TABLE survey_template_scale
    ADD CONSTRAINT uc_survey_template_scale_question UNIQUE (question_id);

ALTER TABLE survey_response_choice
    ADD CONSTRAINT FK_SURVEY_RESPONSE_CHOICE_ON_RESPONSE FOREIGN KEY (response_id) REFERENCES survey_response (response_id);

ALTER TABLE survey_response_matrix
    ADD CONSTRAINT FK_SURVEY_RESPONSE_MATRIX_ON_RESPONSE FOREIGN KEY (response_id) REFERENCES survey_response (response_id);

ALTER TABLE survey_response
    ADD CONSTRAINT FK_SURVEY_RESPONSE_ON_QUESTION FOREIGN KEY (question_id) REFERENCES survey_template (question_id);

ALTER TABLE survey_response
    ADD CONSTRAINT FK_SURVEY_RESPONSE_ON_SURVEY FOREIGN KEY (survey_id) REFERENCES survey (survey_id);

ALTER TABLE survey_template_option
    ADD CONSTRAINT FK_SURVEY_TEMPLATE_OPTION_ON_QUESTION FOREIGN KEY (question_id) REFERENCES survey_template (question_id);

ALTER TABLE survey_template_scale
    ADD CONSTRAINT FK_SURVEY_TEMPLATE_SCALE_ON_QUESTION FOREIGN KEY (question_id) REFERENCES survey_template (question_id);

-- 1. 성별 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type,
                             max_selections, required, sort_order)
VALUES (1, 'gender', '성별을 선택해주세요', 'SINGLE_CHOICE', null, true, 1);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES (1, '여성', 0),
       (1, '남성', 1),
       (1, '선택안함', 2);

-- 2. 나이 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type,
                             max_selections, required, sort_order)
VALUES (2, 'age', '나이는 어떻게 되시나요?', 'NUMERIC_INPUT', null, true, 2);

INSERT INTO survey_template_scale (question_id, min_value, max_value)
VALUES (2, 0, 100);

-- 3. MBTI 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type,
                             max_selections, required, sort_order)
VALUES (3, 'mbti', '당신의 MBTI 유형은 무엇인가요? (설명 확인)', 'SINGLE_CHOICE', null, true, 3);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES (3, 'ISTJ - 세심한 계획자', 0),
       (3, 'ISFJ - 조용한 돌봄이', 1),
       (3, 'INFJ - 통찰력 있는 옹호자', 2),
       (3, 'INTJ - 전략적 사고가', 3),
       (3, 'ISTP - 만능 재주꾼', 4),
       (3, 'ISFP - 온화한 예술가', 5),
       (3, 'INFP - 이상주의자', 6),
       (3, 'INTP - 논리적 분석가', 7),
       (3, 'ESTP - 모험을 좋아하는 사업가', 8),
       (3, 'ESFP - 분위기 메이커', 9),
       (3, 'ENFP - 열정적 활동가', 10),
       (3, 'ENTP - 도전적 발명가', 11),
       (3, 'ESTJ - 체계적인 관리자', 12),
       (3, 'ESFJ - 사교적인 친선도우미', 13),
       (3, 'ENFJ - 카리스마 리더', 14),
       (3, 'ENTJ - 대담한 통솔자', 15);

-- 4. 활동 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type,
                             max_selections, required, sort_order)
VALUES (4, 'activities', '좋아하는 활동은 무엇인가요? (최대 5개 선택)', 'MULTIPLE_CHOICE', 5, true, 4);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES (4, '여행', 0),
       (4, '넷플릭스 감상', 1),
       (4, '집돌이 / 집순이', 2),
       (4, '헬스장', 3),
       (4, '전시회 관람', 4),
       (4, '등산 / 캠핑', 5),
       (4, '친구 만남', 6),
       (4, '게임', 7),
       (4, '온라인 쇼핑', 8),
       (4, '예능 시청', 9),
       (4, '음악 감상', 10),
       (4, '명상/요가', 11),
       (4, '독서', 12),
       (4, '낚시', 13),
       (4, '맛집탐방', 14),
       (4, '영화 감상', 15);

-- 5. 스타일 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type,
                             max_selections, required, sort_order)
VALUES (5, 'style', '자신의 스타일을 어떻게 설명하시나요?', 'SINGLE_CHOICE', null, true, 5);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES (5, '비즈니스룩', 0),
       (5, '힙스터', 1),
       (5, '캠퍼스패션', 2),
       (5, '클래식', 3),
       (5, '섹시', 4),
       (5, '캐주얼', 5),
       (5, '스포티', 6),
       (5, '도시적', 7),
       (5, '유행 따라', 8),
       (5, '락커', 9),
       (5, '자유분방', 10),
       (5, '내추럴', 11),
       (5, '중성적', 12),
       (5, '빈티지', 13),
       (5, '예술가', 14);

-- 6. 색상 선택 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type,
                             max_selections, required, sort_order)
VALUES (6, 'favorite_color', '당신이 가장 끌리는 색상을 선택해주세요 (아래 색상표에서 클릭)', 'COLOR_PICKER', null, true, 6);

-- 7. 향 유형 질문 (MATRIX_SLIDER)
INSERT INTO survey_template (question_id, question_key, question_text, question_type,
                             max_selections, required, sort_order)
VALUES (7, 'desired_fragrance_type', '선호하는 향의 유형을 선택해주세요', 'MATRIX_SLIDER', null, true, 7);

INSERT INTO survey_template_scale (question_id, min_value, max_value)
VALUES (7, 0, 100);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES (7,
        '{"subcats": [{"name": "플로럴", "examples": "신선한 꽃 향기 (예: 장미, 자스민, 라일락)"}, {"name": "소프트 플로럴", "examples": "부드러운 가루 향 (예: 아이리스, 헬리오트로프, 베이비파우더)"}, {"name": "플로럴 앰버", "examples": "꽃향기+달콤함 (예: 오렌지 꽃, 계피, 바닐라)"}], "dimension": "플로럴"}',
        0),
       (7,
        '{"subcats": [{"name": "소프트 앰버", "examples": "은은한 향료 (예: 유향, 몰약, 앰버그리스)"}, {"name": "앰버", "examples": "관능적인 달콤함 (예: 바닐라, 토닉, 레진)"}, {"name": "우디 앰버", "examples": "나무+향료 조합 (예: 백단향(샌달우드), 파출리(패츌리), 오크모스)"}], "dimension": "앰버(오리엔탈)"}',
        1),
       (7,
        '{"subcats": [{"name": "우즈", "examples": "청량한 나무 (예: 삼나무, 갈대(베티버), 테크우드)"}, {"name": "모스 우즈", "examples": "축축한 이끼 느낌 (예: 오크모스, 이끼, 앰버)"}, {"name": "드라이 우즈", "examples": "건조하고 강한 남성미 (예: 가죽, 담배잎, 번트우드)"}], "dimension": "우디"}',
        2),
       (7,
        '{"subcats": [{"name": "아로마틱", "examples": "허브 정원 느낌 (예: 라벤더, 로즈마리, 박하)"}, {"name": "시트러스", "examples": "상쾌한 과일 (예: 레몬, 자몽, 베르가못)"}, {"name": "워터(아쿠아틱)", "examples": "물/바다 느낌 (예: 바다, 산소방울, 수영장)"}, {"name": "그린", "examples": "풀숲 향 (예: 갈반움, 대나무, 신선한 잎사귀)"}, {"name": "프루티", "examples": "달콤한 과일 (예: 복숭아, 망고, 열대과일)"}], "dimension": "프레시"}',
        3);

-- 8. 향수 사용 시간 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type,
                             max_selections, required, sort_order)
VALUES (8, 'perfume_usage_time', '주로 언제 향수를 사용하시나요?', 'SINGLE_CHOICE', null, true, 8);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES (8, '아침/낮', 0),
       (8, '저녁/밤', 1),
       (8, '상관없음', 2);

-- 9. 계절 선호도 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type,
                             max_selections, required, sort_order)
VALUES (9, 'perfume_season_preference', '어떤 계절에 어울리는 향을 더 좋아하시나요?', 'SINGLE_CHOICE', null, true, 9);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES (9, '봄', 0),
       (9, '여름', 1),
       (9, '가을', 2),
       (9, '겨울', 3),
       (9, '계절 상관없음', 4);


-- 10 향수 사용 빈도 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type,
                             max_selections, required, sort_order)
VALUES (10, 'PERFUME_USAGE_FREQUENCY', '일주일에 향수를 얼마나 자주 사용하시나요?', 'SINGLE_CHOICE', 1, true, 10);
INSERT INTO survey_template_option (option_id, question_id, option_text, sort_order)
VALUES (117, 10, '매일 사용 (6-7일)', 1);
INSERT INTO survey_template_option (option_id, question_id, option_text, sort_order)
VALUES (118, 10, '자주 사용 (4-5일)', 2);
INSERT INTO survey_template_option (option_id, question_id, option_text, sort_order)
VALUES (119, 10, '가끔 사용 (2-3일)', 3);
INSERT INTO survey_template_option (option_id, question_id, option_text, sort_order)
VALUES (120, 10, '드물게 사용 (1일 이하)', 4);
INSERT INTO survey_template_option (option_id, question_id, option_text, sort_order)
VALUES (121, 10, '특별한 날에만 사용', 5);
INSERT INTO survey_template_option (option_id, question_id, option_text, sort_order)
VALUES (122, 10, '사용하지 않음', 6);

-- 11 향수 구매 빈도
INSERT INTO survey_template (question_id, question_key, question_text, question_type,
                             max_selections, required, sort_order)
VALUES (11, 'PERFUME_PURCHASE_FREQUENCY', '향수를 구매하신 횟수는 몇 번인가요?', 'SINGLE_CHOICE', 1, true, 11);

INSERT INTO survey_template_option (option_id, question_id, option_text, sort_order)
VALUES (123, 11, '1회', 1);
INSERT INTO survey_template_option (option_id, question_id, option_text, sort_order)
VALUES (124, 11, '2 ~ 5회', 2);
INSERT INTO survey_template_option (option_id, question_id, option_text, sort_order)
VALUES (125, 11, '6 ~ 10회', 3);
INSERT INTO survey_template_option (option_id, question_id, option_text, sort_order)
VALUES (126, 11, '11회 이상', 4);


-- 12. 향수 평가 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type,
                             max_selections, required, sort_order)
VALUES (12, 'past_perfume_ratings', '사용해 본 향수에 대한 만족도를 평가해주세요', 'PERFUME_RATING_SLIDER', null, true,
        10);

INSERT INTO survey_template_scale (question_id, min_value, max_value, step_value, labels)
VALUES (12, 0, 5, 0.5, '["매우 불만족", "보통", "매우 만족"]');
