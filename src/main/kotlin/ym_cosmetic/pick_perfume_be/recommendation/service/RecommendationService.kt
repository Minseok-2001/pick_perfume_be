package ym_cosmetic.pick_perfume_be.recommendation.service

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.event.PerfumeViewedEvent
import ym_cosmetic.pick_perfume_be.common.event.RecommendationClickedEvent
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.recommendation.event.RecommendationImpressionEvent
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchResult
import ym_cosmetic.pick_perfume_be.search.service.PerfumeSearchService
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class RecommendationService(
    private val perfumeRepository: PerfumeRepository,
    private val perfumeSearchService: PerfumeSearchService,
    private val memberPreferenceService: MemberPreferenceService,
    private val eventPublisher: ApplicationEventPublisher
) {
    /**
     * 사용자 맞춤 추천 - 코루틴 활용
     */
    suspend fun getPersonalizedRecommendations(
        memberId: Long,
        limit: Int = 10
    ): List<PerfumeSummaryResponse> = coroutineScope {

        // 사용자 선호도 가져오기 (캐시에 있으면 캐시에서, 없으면 계산 후 캐시 저장)
        val preferences = memberPreferenceService.getMemberPreferences(memberId)

        // 선호도 데이터 검증
        if (preferences.preferredNotes.isEmpty() &&
            preferences.preferredAccords.isEmpty() &&
            preferences.preferredBrands.isEmpty()
        ) {
            // 선호도 정보가 없으면 인기 향수 추천
            return@coroutineScope getPopularPerfumes(limit)
        }

        // 선호도 기반 추천 (비동기)
        val recommendedPerfumesDeferred = async {
            perfumeSearchService.findRecommendedPerfumes(
                memberPreferences = preferences,
                limit = limit,
            )
        }

        // 백그라운드에서 선호도 업데이트 (필요 시)
        if (preferences.lastUpdated.isBefore(LocalDateTime.now().minusHours(24))) {
            launch {
                memberPreferenceService.updateMemberPreferences(memberId)
            }
        }

        // 검색 결과 가져오기
        val recommendedPerfumes = recommendedPerfumesDeferred.await()

        // 검색 결과를 PerfumeSummaryResponse로 변환
        val result = convertToPerfumeSummaryResponses(recommendedPerfumes)

        // 추천 노출 이벤트 발행 (비동기)
        launch {
            result.forEach { perfume ->
                eventPublisher.publishEvent(
                    RecommendationImpressionEvent(
                        memberId = memberId,
                        perfumeId = perfume.id,
                        recommendationType = "personalized"
                    )
                )
            }
        }

        return@coroutineScope result
    }

    /**
     * 유사한 향수 추천
     */
    @Transactional(readOnly = true)
    suspend fun getSimilarPerfumes(
        memberId: Long,
        perfumeId: Long,
        limit: Int = 5
    ): List<PerfumeSummaryResponse> = coroutineScope {
        // 조회 이벤트 발행 (비동기)
        launch {
            eventPublisher.publishEvent(
                PerfumeViewedEvent(
                    memberId = memberId,
                    perfumeId = perfumeId
                )
            )
        }

        // 유사한 향수 검색 (비동기)
        val similarPerfumesDeferred = async {
            perfumeSearchService.findSimilarPerfumes(perfumeId.toString(), limit)
        }

        val similarPerfumes = similarPerfumesDeferred.await()
        val result = convertToPerfumeSummaryResponses(similarPerfumes)

        // 추천 노출 이벤트 발행 (비동기)
        launch {
            result.forEach { perfume ->
                eventPublisher.publishEvent(
                    RecommendationImpressionEvent(
                        memberId = memberId,
                        perfumeId = perfume.id,
                        recommendationType = "similar"
                    )
                )
            }
        }

        return@coroutineScope result
    }

    /**
     * 인기 있는 향수 가져오기
     */
    @Transactional(readOnly = true)
    fun getPopularPerfumes(limit: Int): List<PerfumeSummaryResponse> {
        val pageable = PageRequest.of(0, limit)
        return perfumeRepository.findTopByReviewCount(pageable)
            .map { PerfumeSummaryResponse.from(it) }
            .content
    }

    /**
     * 브랜드 기반 추천
     */
    @Transactional(readOnly = true)
    suspend fun getRecommendationsByBrand(
        memberId: Long,
        brandName: String,
        limit: Int = 10
    ): List<PerfumeSummaryResponse> = coroutineScope {
        val pageable = PageRequest.of(0, limit)

        val perfumesDeferred = async {
            perfumeRepository.findByBrandNameOrderByAverageRatingDesc(brandName, pageable)
                .map { PerfumeSummaryResponse.from(it) }
                .content
        }

        val result = perfumesDeferred.await()

        // 추천 노출 이벤트 발행 (비동기)
        launch {
            result.forEach { perfume ->
                eventPublisher.publishEvent(
                    RecommendationImpressionEvent(
                        memberId = memberId,
                        perfumeId = perfume.id,
                        recommendationType = "brand"
                    )
                )
            }
        }

        return@coroutineScope result
    }

    /**
     * 노트 기반 추천
     */
    @Transactional(readOnly = true)
    suspend fun getRecommendationsByNote(
        memberId: Long,
        noteName: String,
        limit: Int = 10
    ): List<PerfumeSummaryResponse> = coroutineScope {
        val searchResultsDeferred = async {
            perfumeSearchService.searchPerfumes(
                keyword = null,
                note = noteName,
                sortBy = "rating",
                limit = limit
            )
        }

        val searchResults = searchResultsDeferred.await()
        val result = convertToPerfumeSummaryResponses(searchResults)

        // 추천 노출 이벤트 발행 (비동기)
        launch {
            result.forEach { perfume ->
                eventPublisher.publishEvent(
                    RecommendationImpressionEvent(
                        memberId = memberId,
                        perfumeId = perfume.id,
                        recommendationType = "note"
                    )
                )
            }
        }

        return@coroutineScope result
    }

    /**
     * 검색 결과를 PerfumeSummaryResponse로 변환
     */
    private fun convertToPerfumeSummaryResponses(searchResults: List<PerfumeSearchResult>): List<PerfumeSummaryResponse> {
        val perfumeIds = searchResults.map { it.id }

        if (perfumeIds.isEmpty()) {
            return emptyList()
        }

        val perfumes = perfumeRepository.findAllById(perfumeIds)

        // ID 기준으로 맵 생성하여 원래 순서 유지
        val perfumesMap = perfumes.associateBy { it.id!! }

        return searchResults.mapNotNull { result ->
            perfumesMap[result.id]?.let { PerfumeSummaryResponse.from(it) }
        }
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
     * 다양한 요소를 종합한 개인화 추천 (하이브리드 접근법)
     * - 사용자 선호도
     * - 인기 향수
     * - 최신 트렌드
     * - 계절 요소
     */
    @Transactional(readOnly = true)
    suspend fun getHybridRecommendations(
        memberId: Long,
        limit: Int = 10
    ): List<PerfumeSummaryResponse> = coroutineScope {
        // 1. 사용자 선호도 기반 추천 (비동기)
        val preferenceBasedRecsDeferred = async {
            val preferences = memberPreferenceService.getMemberPreferences(memberId)

            if (preferences.preferredNotes.isEmpty() &&
                preferences.preferredAccords.isEmpty() &&
                preferences.preferredBrands.isEmpty()
            ) {
                emptyList<PerfumeSearchResult>()
            } else {
                perfumeSearchService.findRecommendedPerfumes(
                    preferences,
                    limit = limit
                )
            }
        }

        // 2. 인기 향수 추천 (비동기)
        val popularRecsDeferred = async {
            val pageable = PageRequest.of(0, limit / 2)
            perfumeRepository.findTopByReviewCount(pageable).content
        }

        // 3. 계절 요소 반영 (비동기)
        val seasonalRecsDeferred = async {
            val currentMonth = LocalDate.now().monthValue
            val season = when (currentMonth) {
                in 3..5 -> "SPRING"
                in 6..8 -> "SUMMER"
                in 9..11 -> "FALL"
                else -> "WINTER"
            }

            // 계절에 맞는 향수 추천
            perfumeSearchService.searchPerfumes(
                keyword = null,
                season = season,
                limit = limit / 2
            )
        }

        // 각 방식의 추천 결과 취합
        val preferenceBasedRecs = preferenceBasedRecsDeferred.await()
        val popularRecs = popularRecsDeferred.await()
        val seasonalRecs = seasonalRecsDeferred.await()

        // 결과 조합 및 중복 제거
        val combinedPerfumeIds = mutableSetOf<Long>()
        val combinedResults = mutableListOf<PerfumeSummaryResponse>()

        // 1. 우선 선호도 기반 추천 추가 (60%)
        val preferenceBasedCount = (limit * 0.6).toInt()
        val convertedPreferenceRecs = convertToPerfumeSummaryResponses(preferenceBasedRecs)

        convertedPreferenceRecs.forEach { perfume ->
            if (combinedPerfumeIds.size < preferenceBasedCount && !combinedPerfumeIds.contains(
                    perfume.id
                )
            ) {
                combinedPerfumeIds.add(perfume.id)
                combinedResults.add(perfume)
            }
        }

        // 2. 인기 향수 추가 (30%)
        val popularCount = (limit * 0.3).toInt()

        popularRecs.forEach { perfume ->
            if (combinedResults.size < preferenceBasedCount + popularCount && !combinedPerfumeIds.contains(
                    perfume.id!!
                )
            ) {
                combinedPerfumeIds.add(perfume.id!!)
                combinedResults.add(PerfumeSummaryResponse.from(perfume))
            }
        }

        // 3. 계절 요소 추가 (10%)
        limit - combinedResults.size
        val convertedSeasonalRecs = convertToPerfumeSummaryResponses(seasonalRecs)

        convertedSeasonalRecs.forEach { perfume ->
            if (combinedResults.size < limit && !combinedPerfumeIds.contains(perfume.id)) {
                combinedPerfumeIds.add(perfume.id)
                combinedResults.add(perfume)
            }
        }

        // 4. 부족한 경우 인기 향수로 채우기
        if (combinedResults.size < limit) {
            val remainingCount = limit - combinedResults.size
            val additionalPopular = getPopularPerfumes(remainingCount * 2)
                .filter { perfume -> !combinedPerfumeIds.contains(perfume.id) }
                .take(remainingCount)

            combinedResults.addAll(additionalPopular)
        }

        // 추천 노출 이벤트 발행 (비동기)
        launch {
            combinedResults.forEach { perfume ->
                eventPublisher.publishEvent(
                    RecommendationImpressionEvent(
                        memberId = memberId,
                        perfumeId = perfume.id,
                        recommendationType = "hybrid"
                    )
                )
            }
        }

        return@coroutineScope combinedResults
    }
}

