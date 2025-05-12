package ym_cosmetic.pick_perfume_be.recommendation.service

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.entity.MemberPreference
import ym_cosmetic.pick_perfume_be.member.repository.MemberPreferenceRepository
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.review.repository.ReviewRepository
import ym_cosmetic.pick_perfume_be.review.vo.Rating
import ym_cosmetic.pick_perfume_be.vote.repository.VoteRepository
import java.time.LocalDateTime

@Service
class MemberPreferenceAnalysisService(
    private val memberRepository: MemberRepository,
    private val reviewRepository: ReviewRepository,
    private val voteRepository: VoteRepository,
    private val perfumeRepository: PerfumeRepository,
    private val userPreferenceRepository: MemberPreferenceRepository,
    private val applicationCoroutineScope: CoroutineScope

) {
    companion object {
        private val logger =
            LoggerFactory.getLogger(MemberPreferenceAnalysisService::class.java)
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        logger.error("Coroutine failed: ${exception.message}", exception)
    }

    // 사용자 선호도 분석 및 저장
    fun analyzeUserPreferences(memberId: Long) {
        applicationCoroutineScope.launch(exceptionHandler) {
            val member = memberRepository.findById(memberId).orElse(null) ?: return@launch

            // 여러 분석 작업을 병렬로 실행
            val reviewedPerfumeIds = async { getReviewedPerfumeIds(member) }
            val preferredNotes = async { analyzePreferredNotes(member) }
            val preferredAccords = async { analyzePreferredAccords(member) }
            val preferredBrands = async { analyzePreferredBrands(member) }

            val preferences = MemberPreference(
                memberId = memberId,
                reviewedPerfumeIds = reviewedPerfumeIds.await(),
                preferredNotes = preferredNotes.await(),
                preferredAccords = preferredAccords.await(),
                preferredBrands = preferredBrands.await(),
                member = member,
                lastUpdated = LocalDateTime.now(),
            )

            // 사용자 선호도 저장
            userPreferenceRepository.save(preferences)
        }
    }

    // 사용자가 리뷰한 향수 ID 목록
    private suspend fun getReviewedPerfumeIds(member: Member): List<Long> {
        return reviewRepository.findByMemberId(member.id!!)
            .map { it.perfume.id!! }
    }

    // 선호하는 노트 분석
    private suspend fun analyzePreferredNotes(member: Member): List<String> {
        // 높은 평점을 준 향수들에서 노트 추출
        val highRatedReviews =
            reviewRepository.findByMemberIdAndRatingValueGreaterThanEqual(member.id!!, 4)
        val perfumeIds = highRatedReviews.map { it.perfume.id!! }

        val perfumes = perfumeRepository.findAllById(perfumeIds)
        val perfumeMap = perfumes.associateBy { it.id!! }

        val noteCounts = mutableMapOf<String, Int>()

        highRatedReviews.forEach { review ->
            val perfume = perfumeMap[review.perfume.id] ?: return@forEach

            perfume.getNotes().forEach { perfumeNote ->
                val noteName = perfumeNote.note.name
                noteCounts[noteName] = (noteCounts[noteName] ?: 0) + 1
            }
        }

        // 각 향수의 노트 수집 및 카운트
        perfumeIds.forEach { perfumeId ->
            val perfume = perfumeRepository.findById(perfumeId).orElse(null) ?: return@forEach

            perfume.getNotes().forEach { perfumeNote ->
                val noteName = perfumeNote.note.name
                noteCounts[noteName] = (noteCounts[noteName] ?: 0) + 1
            }
        }

        // 카운트 기준으로 정렬하여 상위 노트 반환
        return noteCounts.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }
    }

    // 선호하는 어코드 분석
    private suspend fun analyzePreferredAccords(member: Member): List<String> {
        val highRatedReviews =
            reviewRepository.findByMemberIdAndRatingGreaterThanEqual(member.id!!, Rating.of(4))
        val perfumeIds = highRatedReviews.map { it.perfume.id!! }

        val accordCounts = mutableMapOf<String, Int>()

        perfumeIds.forEach { perfumeId ->
            val perfume = perfumeRepository.findById(perfumeId).orElse(null) ?: return@forEach

            perfume.getAccords().forEach { perfumeAccord ->
                val accordName = perfumeAccord.accord.name
                accordCounts[accordName] = (accordCounts[accordName] ?: 0) + 1
            }
        }

        return accordCounts.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }
    }

    // 선호하는 브랜드 분석
    private suspend fun analyzePreferredBrands(member: Member): List<String> {
        val highRatedReviews =
            reviewRepository.findByMemberIdAndRatingGreaterThanEqual(member.id!!, Rating.of(4))

        val brandCounts = mutableMapOf<String, Int>()

        highRatedReviews.forEach { review ->
            val brandName = review.perfume.brand.name
            brandCounts[brandName] = (brandCounts[brandName] ?: 0) + 1
        }

        return brandCounts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }
    }

    // 모든 사용자의 선호도 분석 (배치 작업)
    fun analyzeAllUserPreferences() {
        applicationCoroutineScope.launch {
            val members = memberRepository.findAll()

            members.forEach { member ->
                try {
                    analyzeUserPreferences(member.id!!)
                } catch (e: Exception) {
                    logger.error(
                        "Failed to analyze preferences for member ${member.id}: ${e.message}",
                        e
                    )
                }
            }
        }
    }
}