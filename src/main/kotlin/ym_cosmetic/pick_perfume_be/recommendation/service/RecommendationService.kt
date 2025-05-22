package ym_cosmetic.pick_perfume_be.recommendation.service

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.event.PerfumeViewedEvent
import ym_cosmetic.pick_perfume_be.common.event.RecommendationClickedEvent
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeLikeRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.recommendation.event.RecommendationImpressionEvent
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchCriteria
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchResult
import ym_cosmetic.pick_perfume_be.search.service.PerfumeSearchService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@Service
class RecommendationService(
    private val perfumeRepository: PerfumeRepository,
    private val perfumeSearchService: PerfumeSearchService,
    private val memberPreferenceService: MemberPreferenceService,
    private val eventPublisher: ApplicationEventPublisher,
    private val perfumeLikeRepository: PerfumeLikeRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(RecommendationService::class.java)
    }

    /**
     * 사용자 맞춤 추천
     */
    @Transactional(readOnly = true)
    fun getPersonalizedRecommendations(
        memberId: Long,
        limit: Int = 10
    ): List<PerfumeSummaryResponse> {
        return try {
            // 1. 사용자 선호도 가져오기
            val preferences = memberPreferenceService.getMemberPreferences(memberId)

            // 2. 선호도 데이터가 충분한지 검증
            val hasPreferences = preferences.preferredNotes.isNotEmpty() ||
                    preferences.preferredAccords.isNotEmpty() ||
                    preferences.preferredBrands.isNotEmpty()

            // 3. 추천 쿼리 실행
            val recommendedPerfumes = if (hasPreferences) {
                perfumeSearchService.findRecommendedPerfumes(
                    memberPreferences = preferences,
                    limit = limit,
                )
            } else {
                emptyList()
            }

            // 4. 백그라운드에서 선호도 업데이트 (비동기로 처리)
            if (preferences.lastUpdated.isBefore(LocalDateTime.now().minusHours(24))) {
                updateMemberPreferencesAsync(memberId)
            }

            // 5. 충분한 추천이 없으면 인기 향수로 보완
            val result = if (recommendedPerfumes.isEmpty()) {
                getPopularPerfumes(limit, memberId)
            } else {
                convertToPerfumeSummaryResponses(recommendedPerfumes, memberId)
            }

            // 6. 추천 노출 이벤트 발행 (비동기)
            publishImpressionEventsAsync(result, memberId, "personalized")

            result
        } catch (e: Exception) {
            logger.error(
                "Error in personalized recommendations for member $memberId: ${e.message}",
                e
            )
            // 에러 발생 시 인기 향수 반환
            getPopularPerfumes(limit, memberId)
        }
    }

    /**
     * 유사한 향수 추천
     */
    @Transactional(readOnly = true)
    fun getSimilarPerfumes(
        memberId: Long?,
        perfumeId: Long,
        limit: Int = 5
    ): List<PerfumeSummaryResponse> {
        // 조회 이벤트 발행 (비동기)
        if (memberId != null) {
            publishEventAsync {
                eventPublisher.publishEvent(
                    PerfumeViewedEvent(
                        memberId = memberId,
                        perfumeId = perfumeId
                    )
                )
            }
        }

        // 유사한 향수 검색
        val similarPerfumes = perfumeSearchService.findSimilarPerfumes(perfumeId.toString(), limit)
        val result = convertToPerfumeSummaryResponses(similarPerfumes, memberId)

        // 추천 노출 이벤트 발행 (비동기)
        if (memberId != null) {
            publishImpressionEventsAsync(result, memberId, "similar")
        }

        return result
    }

    /**
     * 인기 있는 향수 가져오기
     */
    @Transactional(readOnly = true)
    fun getPopularPerfumes(limit: Int, memberId: Long? = null): List<PerfumeSummaryResponse> {
        val pageable = PageRequest.of(0, limit)
        val perfumes = perfumeRepository.findTopByReviewCount(pageable).content

        // 회원이 좋아요한 향수 ID 목록 조회
        val likedPerfumeIds = if (memberId != null) {
            perfumeLikeRepository.findPerfumeIdsByMemberId(memberId)
        } else {
            emptySet()
        }

        return perfumes.map { perfume ->
            PerfumeSummaryResponse.from(
                perfume = perfume,
                isLiked = likedPerfumeIds.contains(perfume.id)
            )
        }
    }

    /**
     * 브랜드 기반 추천
     */
    @Transactional(readOnly = true)
    fun getRecommendationsByBrand(
        memberId: Long,
        brandName: String,
        limit: Int = 10
    ): List<PerfumeSummaryResponse> {
        val pageable = PageRequest.of(0, limit)

        // 회원이 좋아요한 향수 ID 목록 조회
        val likedPerfumeIds = perfumeLikeRepository.findPerfumeIdsByMemberId(memberId)

        val perfumes = perfumeRepository.findByBrandNameOrderByAverageRatingDesc(
            brandName,
            pageable
        ).content

        val result = perfumes.map { perfume ->
            PerfumeSummaryResponse.from(
                perfume = perfume,
                isLiked = likedPerfumeIds.contains(perfume.id)
            )
        }

        // 추천 노출 이벤트 발행 (비동기)
        publishImpressionEventsAsync(result, memberId, "brand")

        return result
    }

    /**
     * 노트 기반 추천
     */
    @Transactional(readOnly = true)
    fun getRecommendationsByNote(
        memberId: Long,
        noteName: String,
        limit: Int = 10
    ): List<PerfumeSummaryResponse> {
        val pageable = PageRequest.of(0, limit)
        val criteria = PerfumeSearchCriteria(
            keyword = null,
            note = noteName,
            sortBy = "rating",
            pageable = pageable
        )

        val searchPageResult = perfumeSearchService.searchPerfumes(criteria)
        val searchResults = searchPageResult.content

        val result = convertToPerfumeSummaryResponses(searchResults, memberId)

        // 추천 노출 이벤트 발행 (비동기)
        publishImpressionEventsAsync(result, memberId, "note")

        return result
    }

    /**
     * 다양한 요소를 종합한 개인화 추천 (하이브리드 접근법)
     */
    @Transactional(readOnly = true)
    fun getHybridRecommendations(
        memberId: Long,
        limit: Int = 10
    ): List<PerfumeSummaryResponse> {
        // 회원이 좋아요한 향수 ID 목록 조회
        val likedPerfumeIds = perfumeLikeRepository.findPerfumeIdsByMemberId(memberId)

        // 1. 사용자 선호도 기반 추천
        val preferences = memberPreferenceService.getMemberPreferences(memberId)
        val preferenceBasedRecs = if (preferences.preferredNotes.isEmpty() &&
            preferences.preferredAccords.isEmpty() &&
            preferences.preferredBrands.isEmpty()
        ) {
            emptyList<PerfumeSearchResult>()
        } else {
            perfumeSearchService.findRecommendedPerfumes(preferences, limit = limit)
        }

        // 2. 인기 향수 추천
        val pageable = PageRequest.of(0, limit / 2)
        val popularRecs = perfumeRepository.findTopByReviewCount(pageable).content

        // 3. 계절 요소 반영
        val currentMonth = LocalDate.now().monthValue
        val season = when (currentMonth) {
            in 3..5 -> "SPRING"
            in 6..8 -> "SUMMER"
            in 9..11 -> "FALL"
            else -> "WINTER"
        }
        val seasonalRecs = searchPerfumesBySeason(season, limit / 2)

        // 결과 조합 및 중복 제거
        val combinedPerfumeIds = mutableSetOf<Long>()
        val combinedResults = mutableListOf<PerfumeSummaryResponse>()

        // 1. 우선 선호도 기반 추천 추가 (60%)
        val preferenceBasedCount = (limit * 0.6).toInt()
        val convertedPreferenceRecs = convertToPerfumeSummaryResponses(preferenceBasedRecs, memberId)

        convertedPreferenceRecs.forEach { perfume ->
            if (combinedPerfumeIds.size < preferenceBasedCount && !combinedPerfumeIds.contains(perfume.id)) {
                combinedPerfumeIds.add(perfume.id)
                combinedResults.add(perfume)
            }
        }

        // 2. 인기 향수 추가 (30%)
        val popularCount = (limit * 0.3).toInt()
        popularRecs.forEach { perfume ->
            if (combinedResults.size < preferenceBasedCount + popularCount &&
                !combinedPerfumeIds.contains(perfume.id!!)) {
                combinedPerfumeIds.add(perfume.id!!)
                combinedResults.add(
                    PerfumeSummaryResponse.from(
                        perfume = perfume,
                        isLiked = likedPerfumeIds.contains(perfume.id)
                    )
                )
            }
        }

        // 3. 계절 요소 추가 (10%)
        val convertedSeasonalRecs = convertToPerfumeSummaryResponses(seasonalRecs, memberId)
        convertedSeasonalRecs.forEach { perfume ->
            if (combinedResults.size < limit && !combinedPerfumeIds.contains(perfume.id)) {
                combinedPerfumeIds.add(perfume.id)
                combinedResults.add(perfume)
            }
        }

        // 4. 부족한 경우 인기 향수로 채우기
        if (combinedResults.size < limit) {
            val remainingCount = limit - combinedResults.size
            val additionalPopular = getPopularPerfumes(remainingCount * 2, memberId)
                .filter { perfume -> !combinedPerfumeIds.contains(perfume.id) }
                .take(remainingCount)

            combinedResults.addAll(additionalPopular)
        }

        // 추천 노출 이벤트 발행 (비동기)
        publishImpressionEventsAsync(combinedResults, memberId, "hybrid")

        return combinedResults
    }

    /**
     * 추천 클릭 기록
     */
    fun recordRecommendationClick(
        memberId: Long,
        perfumeId: Long,
        recommendationType: String
    ) {
        eventPublisher.publishEvent(
            RecommendationClickedEvent(
                memberId = memberId,
                perfumeId = perfumeId,
                recommendationType = recommendationType
            )
        )
    }

    /**
     * 검색 결과를 PerfumeSummaryResponse로 변환
     */
    private fun convertToPerfumeSummaryResponses(
        searchResults: List<PerfumeSearchResult>,
        memberId: Long? = null
    ): List<PerfumeSummaryResponse> {
        if (searchResults.isEmpty()) {
            return emptyList()
        }

        // 검색 결과에서 향수 ID 목록 추출
        val perfumeIds = searchResults.map { it.id }

        // 회원이 좋아요한 향수 ID 목록 조회
        val likedPerfumeIds = if (memberId != null) {
            perfumeLikeRepository.findPerfumeIdsByMemberId(memberId)
        } else {
            emptySet()
        }

        return try {
            // FetchJoin으로 브랜드 및 관련 정보를 함께 조회
            val perfumes = perfumeRepository.findAllByIdsWithBrand(perfumeIds)

            // ID 기준으로 맵 생성하여 원래 순서 유지
            val perfumesMap = perfumes.associateBy { it.id!! }

            searchResults.mapNotNull { result ->
                perfumesMap[result.id]?.let {
                    PerfumeSummaryResponse.from(
                        perfume = it,
                        isLiked = likedPerfumeIds.contains(it.id)
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("Error converting search results to responses: ${e.message}", e)

            // 에러 발생 시 ID로 개별 조회하여 변환 시도
            searchResults.mapNotNull { result ->
                try {
                    perfumeRepository.findById(result.id).orElse(null)?.let {
                        PerfumeSummaryResponse.from(
                            perfume = it,
                            isLiked = likedPerfumeIds.contains(it.id)
                        )
                    }
                } catch (ex: Exception) {
                    logger.error("Failed to convert perfume ID ${result.id}: ${ex.message}")
                    null
                }
            }
        }
    }

    /**
     * 시즌 기반 향수 검색
     */
    private fun searchPerfumesBySeason(season: String, limit: Int): List<PerfumeSearchResult> {
        val pageable = PageRequest.of(0, limit)
        val criteria = PerfumeSearchCriteria(
            keyword = null,
            sortBy = "rating",
            pageable = pageable,
            season = season
        )

        val searchPageResult = perfumeSearchService.searchPerfumes(criteria)
        return searchPageResult.content
    }

    // === 비동기 헬퍼 메소드들 ===

    /**
     * 회원 선호도 업데이트를 백그라운드에서 실행
     */
    private fun updateMemberPreferencesAsync(memberId: Long) {
        CompletableFuture.runAsync {
            try {
                memberPreferenceService.updateMemberPreferences(memberId)
            } catch (e: Exception) {
                logger.error("Failed to update member preferences: ${e.message}", e)
            }
        }
    }

    /**
     * 추천 노출 이벤트들을 백그라운드에서 발행
     */
    private fun publishImpressionEventsAsync(
        perfumes: List<PerfumeSummaryResponse>,
        memberId: Long,
        recommendationType: String
    ) {
        CompletableFuture.runAsync {
            perfumes.forEach { perfume ->
                try {
                    eventPublisher.publishEvent(
                        RecommendationImpressionEvent(
                            memberId = memberId,
                            perfumeId = perfume.id,
                            recommendationType = recommendationType
                        )
                    )
                } catch (e: Exception) {
                    logger.error("Failed to publish impression event: ${e.message}", e)
                }
            }
        }
    }

    /**
     * 일반적인 이벤트를 백그라운드에서 발행
     */
    private fun publishEventAsync(eventAction: () -> Unit) {
        CompletableFuture.runAsync {
            try {
                eventAction()
            } catch (e: Exception) {
                logger.error("Failed to publish event: ${e.message}", e)
            }
        }
    }
}