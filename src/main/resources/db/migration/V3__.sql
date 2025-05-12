-- 1. 성별 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type, max_selections, required, sort_order)
VALUES (1, 'gender', '성별을 선택해주세요', 'SINGLE_CHOICE', null, true, 1);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES (1, '여성', 0), (1, '남성', 1), (1, '선택안함', 2);

-- 2. 나이 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type, max_selections, required, sort_order)
VALUES (2, 'age', '나이는 어떻게 되시나요?', 'NUMERIC_INPUT', null, true, 2);

INSERT INTO survey_template_scale (question_id, min_value, max_value)
VALUES (2, 0, 100);

-- 3. MBTI 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type, max_selections, required, sort_order)
VALUES (3, 'mbti', '당신의 MBTI 유형은 무엇인가요? (설명 확인)', 'SINGLE_CHOICE', null, true, 3);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES
    (3, 'ISTJ - 세심한 계획자', 0),
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
INSERT INTO survey_template (question_id, question_key, question_text, question_type, max_selections, required, sort_order)
VALUES (4, 'activities', '좋아하는 활동은 무엇인가요? (최대 5개 선택)', 'MULTIPLE_CHOICE', 5, true, 4);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES
    (4, '여행', 0),
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
INSERT INTO survey_template (question_id, question_key, question_text, question_type, max_selections, required, sort_order)
VALUES (5, 'style', '자신의 스타일을 어떻게 설명하시나요?', 'SINGLE_CHOICE', null, true, 5);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES
    (5, '비즈니스룩', 0),
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
INSERT INTO survey_template (question_id, question_key, question_text, question_type, max_selections, required, sort_order)
VALUES (6, 'favorite_color', '당신이 가장 끌리는 색상을 선택해주세요 (아래 색상표에서 클릭)', 'COLOR_PICKER', null, true, 6);

-- 7. 향 유형 질문 (MATRIX_SLIDER)
INSERT INTO survey_template (question_id, question_key, question_text, question_type, max_selections, required, sort_order)
VALUES (7, 'desired_fragrance_type', '선호하는 향의 유형을 선택해주세요', 'MATRIX_SLIDER', null, true, 7);

INSERT INTO survey_template_scale (question_id, min_value, max_value)
VALUES (7, 0, 100);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES
    (7, '{"subcats": [{"name": "플로럴", "examples": "신선한 꽃 향기 (예: 장미, 자스민, 라일락)"}, {"name": "소프트 플로럴", "examples": "부드러운 가루 향 (예: 아이리스, 헬리오트로프, 베이비파우더)"}, {"name": "플로럴 앰버", "examples": "꽃향기+달콤함 (예: 오렌지 꽃, 계피, 바닐라)"}], "dimension": "플로럴"}', 0),
    (7, '{"subcats": [{"name": "소프트 앰버", "examples": "은은한 향료 (예: 유향, 몰약, 앰버그리스)"}, {"name": "앰버", "examples": "관능적인 달콤함 (예: 바닐라, 토닉, 레진)"}, {"name": "우디 앰버", "examples": "나무+향료 조합 (예: 백단향(샌달우드), 파출리(패츌리), 오크모스)"}], "dimension": "앰버(오리엔탈)"}', 1),
    (7, '{"subcats": [{"name": "우즈", "examples": "청량한 나무 (예: 삼나무, 갈대(베티버), 테크우드)"}, {"name": "모스 우즈", "examples": "축축한 이끼 느낌 (예: 오크모스, 이끼, 앰버)"}, {"name": "드라이 우즈", "examples": "건조하고 강한 남성미 (예: 가죽, 담배잎, 번트우드)"}], "dimension": "우디"}', 2),
    (7, '{"subcats": [{"name": "아로마틱", "examples": "허브 정원 느낌 (예: 라벤더, 로즈마리, 박하)"}, {"name": "시트러스", "examples": "상쾌한 과일 (예: 레몬, 자몽, 베르가못)"}, {"name": "워터(아쿠아틱)", "examples": "물/바다 느낌 (예: 바다, 산소방울, 수영장)"}, {"name": "그린", "examples": "풀숲 향 (예: 갈반움, 대나무, 신선한 잎사귀)"}, {"name": "프루티", "examples": "달콤한 과일 (예: 복숭아, 망고, 열대과일)"}], "dimension": "프레시"}', 3);

-- 8. 향수 사용 시간 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type, max_selections, required, sort_order)
VALUES (8, 'perfume_usage_time', '주로 언제 향수를 사용하시나요?', 'SINGLE_CHOICE', null, true, 8);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES
    (8, '아침/낮', 0),
    (8, '저녁/밤', 1),
    (8, '상관없음', 2);

-- 9. 계절 선호도 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type, max_selections, required, sort_order)
VALUES (9, 'perfume_season_preference', '어떤 계절에 어울리는 향을 더 좋아하시나요?', 'SINGLE_CHOICE', null, true, 9);

INSERT INTO survey_template_option (question_id, option_text, sort_order)
VALUES
    (9, '봄', 0),
    (9, '여름', 1),
    (9, '가을', 2),
    (9, '겨울', 3),
    (9, '계절 상관없음', 4);

-- 10. 향수 평가 질문
INSERT INTO survey_template (question_id, question_key, question_text, question_type, max_selections, required, sort_order)
VALUES (10, 'past_perfume_ratings', '사용해 본 향수에 대한 만족도를 평가해주세요', 'PERFUME_RATING_SLIDER', null, true, 10);

INSERT INTO survey_template_scale (question_id, min_value, max_value, step_value, labels)
VALUES (10, 0, 5, 0.5, '["매우 불만족", "보통", "매우 만족"]');