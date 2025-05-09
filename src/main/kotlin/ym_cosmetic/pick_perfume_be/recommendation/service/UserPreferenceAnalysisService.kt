package ym_cosmetic.pick_perfume_be.recommendation.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.repository.MemberPreferenceRepository
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.review.repository.ReviewRepository
import ym_cosmetic.pick_perfume_be.vote.repository.VoteRepository

@Service
class UserPreferenceAnalysisService(
    private val memberRepository: MemberRepository,
    private val reviewRepository: ReviewRepository,
    private val voteRepository: VoteRepository,
    private val perfumeRepository: PerfumeRepository,
    private val userPreferenceRepository: MemberPreferenceRepository
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // 사용자 선호도 분석 및 저장
    fun analyzeUserPreferences(userId: Long) {
        coroutineScope.launch {
            val member = memberRepository.findById(userId).orElse(null) ?: return@launch

            // 여러 분석 작업을 병렬로 실행
            val reviewedPerfumeIds = async { getReviewedPerfumeIds(member) }
            val preferredNotes = async { analyzePreferredNotes(member) }
            val preferredAccords = async { analyzePreferredAccords(member) }
            val preferredBrands = async { analyzePreferredBrands(member) }

            // 분석 결과 취합
            val preferences = UserPreferences(
                userId = userId,
                reviewedPerfumeIds = reviewedPerfumeIds.await(),
                preferredNotes = preferredNotes.await(),
                preferredAccords = preferredAccords.await(),
                preferredBrands = preferredBrands.await()
            )

            // 사용자 선호도 저장
            userPreferenceRepository.save(preferences)
        }
    }

    // 사용자가 리뷰한 향수 ID 목록
    private suspend fun getReviewedPerfumeIds(member: Member): List<String> {
        return reviewRepository.findByMemberId(member.id!!)
            .map { it.perfume.id.toString() }
    }

    // 선호하는 노트 분석
    private suspend fun analyzePreferredNotes(member: Member): List<String> {
        // 높은 평점을 준 향수들에서 노트 추출
        val highRatedReviews =
            reviewRepository.findByMemberIdAndRatingGreaterThanEqual(member.id!!, 4)
        val perfumeIds = highRatedReviews.map { it.perfume.id!! }

        val noteCounts = mutableMapOf<String, Int>()

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
            reviewRepository.findByMemberIdAndRatingGreaterThanEqual(member.id!!, 4)
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
            reviewRepository.findByMemberIdAndRatingGreaterThanEqual(member.id!!, 4)

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
        coroutineScope.launch {
            val members = memberRepository.findAll()

            members.forEach { member ->
                try {
                    analyzeUserPreferences(member.id!!)
                } catch (e: Exception) {
                    // 로깅 처리
                }
            }
        }
    }
}