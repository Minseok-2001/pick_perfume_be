package ym_cosmetic.pick_perfume_be.recommendation.service

import org.opensearch.data.client.orhlc.NativeSearchQueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.index.query.functionscore.ScoreFunctionBuilders
import org.opensearch.search.sort.SortBuilders
import org.opensearch.search.sort.SortOrder
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeNote
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeLikeRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchResult
import ym_cosmetic.pick_perfume_be.survey.entity.QuestionType
import ym_cosmetic.pick_perfume_be.survey.repository.SurveyRepository
import ym_cosmetic.pick_perfume_be.survey.repository.SurveyResponsePerfumeRatingRepository
import ym_cosmetic.pick_perfume_be.survey.repository.SurveyResponseRepository

@Service
class SurveyRecommendationService(
    private val surveyRepository: SurveyRepository,
    private val surveyResponseRepository: SurveyResponseRepository,
    private val surveyResponsePerfumeRatingRepository: SurveyResponsePerfumeRatingRepository,
    private val perfumeRepository: PerfumeRepository,
    private val perfumeLikeRepository: PerfumeLikeRepository,
    private val openSearchOperations: ElasticsearchOperations
) {
    companion object {
        private val logger = LoggerFactory.getLogger(SurveyRecommendationService::class.java)
        
        // 설문 질문별 가중치 설정
        private val QUESTION_WEIGHTS = mapOf(
            "gender" to 1.5f,                        // 성별
            "age" to 0.5f,                           // 나이
            "mbti" to 0.8f,                          // MBTI
            "activities" to 1.0f,                    // 활동
            "style" to 1.2f,                         // 스타일
            "favorite_color" to 0.7f,                // 색상
            "desired_fragrance_type" to 2.0f,        // 향 유형 (가장 중요)
            "perfume_usage_time" to 1.0f,            // 사용 시간
            "perfume_season_preference" to 1.5f,     // 계절 선호도
            "PERFUME_USAGE_FREQUENCY" to 0.6f,       // 사용 빈도
            "PERFUME_PURCHASE_FREQUENCY" to 0.5f,    // 구매 빈도
            "past_perfume_ratings" to 1.8f           // 향수 평점 (두 번째로 중요)
        )
        
        // 향 유형 카테고리 매핑
        private val FRAGRANCE_TYPE_MAPPING = mapOf(
            "플로럴" to listOf("floral", "flower", "rose", "jasmine", "lily"),
            "소프트 플로럴" to listOf("powder", "iris", "heliotrope"),
            "플로럴 앰버" to listOf("orange blossom", "cinnamon", "vanilla"),
            "소프트 앰버" to listOf("incense", "myrrh", "ambergris"),
            "앰버" to listOf("vanilla", "tonic", "resin", "amber"),
            "우디 앰버" to listOf("sandalwood", "patchouli", "oakmoss"),
            "우즈" to listOf("cedar", "vetiver", "teak"),
            "모스 우즈" to listOf("oakmoss", "moss", "amber"),
            "드라이 우즈" to listOf("leather", "tobacco", "burnt wood"),
            "아로마틱" to listOf("lavender", "rosemary", "mint"),
            "시트러스" to listOf("lemon", "grapefruit", "bergamot", "citrus"),
            "워터(아쿠아틱)" to listOf("sea", "marine", "aquatic", "water"),
            "그린" to listOf("galbanum", "bamboo", "leaf", "green"),
            "프루티" to listOf("peach", "mango", "fruit", "fruity")
        )
        
        // 계절 매핑
        private val SEASON_MAPPING = mapOf(
            "봄" to "spring",
            "여름" to "summer",
            "가을" to "fall",
            "겨울" to "winter"
        )
    }

    /**
     * 설문 기반 향수 추천
     * 
     * @param surveyId 설문 ID
     * @param limit 추천 개수
     * @return 추천된 향수 목록
     */
    @Transactional(readOnly = true)
    fun getRecommendationsBySurvey(surveyId: Long, limit: Int): List<PerfumeSummaryResponse> {
        // 1. 설문 조회
        val survey = surveyRepository.findById(surveyId)
            .orElseThrow { NoSuchElementException("설문을 찾을 수 없습니다: $surveyId") }
        
        // 2. 설문 응답 분석
        val responses = surveyResponseRepository.findBySurveySurveyId(surveyId)
        
        // 3. 향수 선호도 분석 및 검색 조건 구성
        val boolQueryBuilder = QueryBuilders.boolQuery()
        
        // 기본 쿼리 - 승인된 향수만 검색
        boolQueryBuilder.must(QueryBuilders.termQuery("isApproved", true))
        
        // 4. 각 질문 유형별 가중치 적용
        responses.forEach { response ->
            val questionKey = response.question.questionKey
            val weight = QUESTION_WEIGHTS[questionKey] ?: 0.5f
            
            when (response.question.questionType) {
                // 성별 기반 추천
                QuestionType.SINGLE_CHOICE -> {
                    when (questionKey) {
                        "gender" -> {
                            if (response.choiceAnswers.isNotEmpty()) {
                                val gender = when (response.choiceAnswers[0].optionText) {
                                    "여성" -> "female"
                                    "남성" -> "male"
                                    else -> null
                                }
                                
                                if (gender != null) {
                                    val genderQuery = QueryBuilders.termQuery("gender", gender)
                                    boolQueryBuilder.should(genderQuery.boost(weight))
                                }
                            }
                        }
                        
                        // 계절 선호도 기반 추천
                        "perfume_season_preference" -> {
                            if (response.choiceAnswers.isNotEmpty()) {
                                val season = response.choiceAnswers[0].optionText
                                if (season != "계절 상관없음") {
                                    val seasonField = SEASON_MAPPING[season]
                                    if (seasonField != null) {
                                        val seasonQuery = QueryBuilders.rangeQuery("seasonality.$seasonField")
                                            .gte(0.6)
                                        boolQueryBuilder.should(seasonQuery.boost(weight))
                                    }
                                }
                            }
                        }
                        
                        // 스타일 기반 추천
                        "style" -> {
                            if (response.choiceAnswers.isNotEmpty()) {
                                val style = response.choiceAnswers[0].optionText
                                val styleQuery = QueryBuilders.matchQuery("content", style)
                                boolQueryBuilder.should(styleQuery.boost(weight * 0.8f))
                            }
                        }
                        
                        // 향수 사용 시간 기반 추천
                        "perfume_usage_time" -> {
                            if (response.choiceAnswers.isNotEmpty()) {
                                val usageTime = response.choiceAnswers[0].optionText
                                val timeQuery = when (usageTime) {
                                    "아침/낮" -> {
                                        QueryBuilders.boolQuery()
                                            .should(QueryBuilders.matchQuery("content", "fresh"))
                                            .should(QueryBuilders.matchQuery("content", "light"))
                                    }
                                    "저녁/밤" -> {
                                        QueryBuilders.boolQuery()
                                            .should(QueryBuilders.matchQuery("content", "intense"))
                                            .should(QueryBuilders.matchQuery("content", "deep"))
                                    }
                                    else -> null
                                }
                                
                                timeQuery?.let {
                                    boolQueryBuilder.should(it.boost(weight))
                                }
                            }
                        }
                    }
                }
                
                // 다중 선택 (활동 등)
                QuestionType.MULTIPLE_CHOICE -> {
                    if (questionKey == "activities" && response.choiceAnswers.isNotEmpty()) {
                        val activitiesQuery = QueryBuilders.boolQuery()
                        response.choiceAnswers.forEach { choice ->
                            activitiesQuery.should(QueryBuilders.matchQuery("content", choice.optionText))
                        }
                        
                        boolQueryBuilder.should(activitiesQuery.boost(weight))
                    }
                }
                
                // 향 유형 선호도 (행렬 슬라이더)
                QuestionType.MATRIX_SLIDER -> {
                    if (questionKey == "desired_fragrance_type" && response.matrixAnswers.isNotEmpty()) {
                        val fragranceTypeQuery = QueryBuilders.boolQuery()
                        
                        response.matrixAnswers.forEach { matrixAnswer ->
                            val value = matrixAnswer.value
                            if (value > 50) { // 50% 이상 선호도만 고려
                                val fragranceType = matrixAnswer.optionKey
                                val normalizedScore = (value - 50) / 50f // 50-100 범위를 0-1로 정규화
                                
                                // 해당 향 유형에 매핑된 키워드 검색
                                FRAGRANCE_TYPE_MAPPING[fragranceType]?.forEach { keyword ->
                                    val keywordQuery = QueryBuilders.boolQuery()
                                        .should(QueryBuilders.matchQuery("notes.name", keyword))
                                        .should(QueryBuilders.matchQuery("accords.name", keyword))
                                        .should(QueryBuilders.matchQuery("content", keyword))
                                    
                                    fragranceTypeQuery.should(keywordQuery)
                                    boolQueryBuilder.should(keywordQuery.boost(weight * normalizedScore))
                                }
                            }
                        }
                        
                        if (fragranceTypeQuery.hasClauses()) {
                            boolQueryBuilder.should(fragranceTypeQuery.boost(weight))
                        }
                    }
                }
                
                // 향수 평점 기반 추천
                QuestionType.PERFUME_RATING_SLIDER -> {
                    if (questionKey == "past_perfume_ratings") {
                        val responseId = response.responseId
                        if (responseId != null) {
                            val perfumeRatings = surveyResponsePerfumeRatingRepository
                                .findByResponseId(responseId)
                            
                            if (perfumeRatings.isNotEmpty()) {
                                // 높은 평점의 향수와 유사한 향수 추천
                                perfumeRatings.filter { it.rating >= 4.0f && it.perfume != null }
                                    .forEach { rating ->
                                        val perfume = rating.perfume
                                        if (perfume != null) {
                                            // 노트 유사성
                                            val notesQuery = QueryBuilders.boolQuery()
                                            perfume.perfumeNotes.forEach { perfumeNote ->
                                                notesQuery.should(
                                                    QueryBuilders.termQuery("notes.name.keyword", perfumeNote.note.name)
                                                )
                                            }
                                            
                                            // 어코드 유사성
                                            val accordsQuery = QueryBuilders.boolQuery()
                                            perfume.perfumeAccords.forEach { perfumeAccord ->
                                                accordsQuery.should(
                                                    QueryBuilders.termQuery("accords.name.keyword", perfumeAccord.accord.name)
                                                )
                                            }
                                            
                                            // 브랜드 유사성
                                            val brandQuery = QueryBuilders.termQuery(
                                                "brandId", perfume.brand.id
                                            )
                                            
                                            // 가중치 적용 (평점에 비례)
                                            val ratingWeight = (rating.rating / 5.0f) * weight
                                            
                                            if (notesQuery.hasClauses()) {
                                                boolQueryBuilder.should(notesQuery.boost(ratingWeight * 1.5f))
                                            }
                                            
                                            if (accordsQuery.hasClauses()) {
                                                boolQueryBuilder.should(accordsQuery.boost(ratingWeight))
                                            }
                                            
                                            boolQueryBuilder.should(brandQuery.boost(ratingWeight * 0.5f))
                                        }
                                    }
                                
                                // 낮은 평점의 향수와 다른 향수 추천 (부정적 가중치)
                                perfumeRatings.filter { it.rating <= 2.0f && it.perfume != null }
                                    .forEach { rating ->
                                        val perfume = rating.perfume
                                        if (perfume != null) {
                                            // 노트 유사성에 부정적 가중치
                                            val notesQuery = QueryBuilders.boolQuery()
                                            perfume.perfumeNotes.forEach { perfumeNote ->
                                                notesQuery.should(
                                                    QueryBuilders.termQuery("notes.name.keyword", perfumeNote.note.name)
                                                )
                                            }
                                            
                                            // 어코드 유사성에 부정적 가중치
                                            val accordsQuery = QueryBuilders.boolQuery()
                                            perfume.perfumeAccords.forEach { perfumeAccord ->
                                                accordsQuery.should(
                                                    QueryBuilders.termQuery("accords.name.keyword", perfumeAccord.accord.name)
                                                )
                                            }
                                            
                                            // 부정적 가중치 적용 (낮은 평점일수록 더 부정적)
                                            val negativeWeight = -((3.0f - rating.rating) / 3.0f) * weight * 0.5f
                                            
                                            if (notesQuery.hasClauses()) {
                                                boolQueryBuilder.should(notesQuery.boost(negativeWeight))
                                            }
                                            
                                            if (accordsQuery.hasClauses()) {
                                                boolQueryBuilder.should(accordsQuery.boost(negativeWeight))
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
                
                else -> {
                    // 다른 질문 유형은 현재 미구현
                    logger.debug("미구현된 질문 유형: ${response.question.questionType}")
                }
            }
        }
        
        // 최소 하나 이상 일치해야 함
        boolQueryBuilder.minimumShouldMatch(1)
        
        // 5. 최종 검색 쿼리 실행
        val searchQuery = NativeSearchQueryBuilder()
            .withQuery(boolQueryBuilder)
            .withSort(SortBuilders.scoreSort().order(SortOrder.DESC))
            .withMaxResults(limit)
            .build()
        
        val searchHits = openSearchOperations.search(
            searchQuery,
            PerfumeDocument::class.java,
            IndexCoordinates.of("perfumes")
        )
        
        // 6. 검색 결과를 PerfumeSummaryResponse로 변환
        val perfumeIds = searchHits.map { it.content.id }
        val perfumes = perfumeRepository.findAllById(perfumeIds)
        
        // 7. 회원 ID가 있는 경우 좋아요 정보 포함
        val memberId = survey.memberId
        val likedPerfumeIds = if (memberId != null) {
            perfumeLikeRepository.findPerfumeIdsByMemberId(memberId)
        } else {
            emptySet()
        }
        
        // 8. 검색 결과 순서대로 정렬하여 반환
        val perfumeMap = perfumes.associateBy { it.id }
        return perfumeIds
            .mapNotNull { perfumeMap[it] }
            .map { perfume ->
                PerfumeSummaryResponse.from(
                    perfume = perfume,
                    isLiked = likedPerfumeIds.contains(perfume.id)
                )
            }
    }
} 