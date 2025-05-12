-- 새 테이블 생성
CREATE TABLE survey_template_option (
    option_id BIGINT AUTO_INCREMENT NOT NULL,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    sort_order INT NOT NULL,
    CONSTRAINT pk_survey_template_option PRIMARY KEY (option_id),
    CONSTRAINT fk_survey_template_option_template FOREIGN KEY (question_id) REFERENCES survey_template (question_id)
);

CREATE TABLE survey_template_scale (
    scale_id BIGINT AUTO_INCREMENT NOT NULL,
    question_id BIGINT NOT NULL,
    min_value INT NOT NULL,
    max_value INT NOT NULL,
    CONSTRAINT pk_survey_template_scale PRIMARY KEY (scale_id),
    CONSTRAINT fk_survey_template_scale_template FOREIGN KEY (question_id) REFERENCES survey_template (question_id),
    CONSTRAINT uk_survey_template_scale_template UNIQUE (question_id)
);

CREATE TABLE survey_response_choice (
    choice_id BIGINT AUTO_INCREMENT NOT NULL,
    response_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    CONSTRAINT pk_survey_response_choice PRIMARY KEY (choice_id),
    CONSTRAINT fk_survey_response_choice_response FOREIGN KEY (response_id) REFERENCES survey_response (response_id)
);

CREATE TABLE survey_response_matrix (
    matrix_id BIGINT AUTO_INCREMENT NOT NULL,
    response_id BIGINT NOT NULL,
    option_key TEXT NOT NULL,
    value INT NOT NULL,
    CONSTRAINT pk_survey_response_matrix PRIMARY KEY (matrix_id),
    CONSTRAINT fk_survey_response_matrix_response FOREIGN KEY (response_id) REFERENCES survey_response (response_id)
);

-- 기존 데이터 마이그레이션 (이 부분은 실제 데이터 구조에 따라 조정 필요)
-- JSON 데이터를 파싱하여 새 테이블에 삽입하는 로직은 애플리케이션 단에서 처리하는 것이 좋을 수 있음

-- 기존 컬럼 제거 (선택적)
ALTER TABLE survey_template DROP COLUMN options;
ALTER TABLE survey_template DROP COLUMN scale;
ALTER TABLE survey_response DROP COLUMN choice_answers;
ALTER TABLE survey_response DROP COLUMN matrix_answers;