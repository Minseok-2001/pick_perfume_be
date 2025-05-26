INSERT INTO survey_template VALUES
    (13, 'fragrance_intensity', '선호하는 향의 강도는?', 'SINGLE_CHOICE', null, true, 13);

INSERT INTO survey_template_option (question_id, option_text, sort_order) VALUES
                                                                              (13, '은은하고 가벼운 향', 0),
                                                                              (13, '적당한 강도', 1),
                                                                              (13, '진하고 강한 향', 2);
INSERT INTO survey_template VALUES
    (14, 'price_range', '향수 구매 시 선호 가격대는?', 'SINGLE_CHOICE', null, true, 14);

INSERT INTO survey_template_option (question_id, option_text, sort_order) VALUES
                                                                              (14, '1만원 이하', 0),
                                                                              (14, '1만원 ~ 5만원', 1),
                                                                              (14, '5만원 ~ 10만원', 2),
                                                                              (14, '10만원 ~ 20만원', 3),
                                                                              (14, '20만원 이상', 4);

INSERT INTO survey_template VALUES
    (16, 'usage_occasion', '주로 어떤 상황에서 향수를 사용하시나요?', 'MULTIPLE_CHOICE', 3, true, 16);

INSERT INTO survey_template_option (question_id, option_text, sort_order) VALUES
                                                                              (16, '데일리/출근', 0),
                                                                              (16, '데이트', 1),
                                                                              (16, '특별한 이벤트', 2),
                                                                              (16, '운동/액티비티', 3),
                                                                              (16, '휴식/집에서', 4),
                                                                              (16, '여행', 5),
                                                                              (16, '기타', 6);
INSERT INTO survey_template_option (question_id, option_text, sort_order) VALUES
                                                                              (2, '18세 이하', 0),
                                                                              (2, '19세 ~ 24세', 1),
                                                                              (2, '25세 ~ 34세', 2),
                                                                              (2, '35세 ~ 44세', 3),
                                                                              (2, '45세 이상', 4);


