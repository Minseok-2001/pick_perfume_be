package ym_cosmetic.pick_perfume_be.recommendation.service

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener
import ym_cosmetic.pick_perfume_be.common.event.*
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.member.dto.MemberPreferenceDto
import ym_cosmetic.pick_perfume_be.member.entity.MemberPreference
import ym_cosmetic.pick_perfume_be.member.repository.MemberPreferenceRepository
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.review.repository.ReviewRepository
import ym_cosmetic.pick_perfume_be.vote.repository.VoteRepository
import java.time.LocalDateTime

@Service
class MemberPreferenceService(
    private val memberPreferenceRepository: MemberPreferenceRepository,
    private val memberRepository: MemberRepository,
    private val reviewRepository: ReviewRepository,
    private val voteRepository: VoteRepository,
    private val perfumeRepository: PerfumeRepository
) {
    /**
     * 회원의 선호도 정보 조회
     *
     * @param memberId 회원 ID
     * @return 회원 선호도 정보
     */
    @Cacheable(cacheNames = ["memberPreferences"], key = "#memberId")
    @Transactional(readOnly = true)
    fun getMemberPreferences(memberId: Long): MemberPreferenceDto {
        val preferences = memberPreferenceRepository.findByMemberId(memberId)
            ?: calculateAndSaveMemberPreferences(memberId)

        return MemberPreferenceDto.from(preferences)
    }

    /**
     * 회원 선호도 계산 및 저장
     *
     * @param memberId 회원 ID
     * @return 계산된 회원 선호도 엔티티
     */
    @Transactional
    fun calculateAndSaveMemberPreferences(memberId: Long): MemberPreference {
        // 회원 존재 확인
        val member = memberRepository.findById(memberId)
            .orElseThrow { EntityNotFoundException("Member not found with id: $memberId") }

        // 선호도 계산
        val preferredNotes = calculatePreferredNotes(memberId)
        val preferredAccords = calculatePreferredAccords(memberId)
        val preferredBrands = calculatePreferredBrands(memberId)
        val reviewedPerfumeIds = getReviewedPerfumeIds(memberId)

        // 기존 선호도 정보 조회 또는 새로 생성
        val preferences = memberPreferenceRepository.findByMemberId(memberId)
            ?: MemberPreference(
                memberId = memberId,
                member = member
            )

        // 선호도 정보 업데이트
        preferences.apply {
            this.preferredNotes = preferredNotes
            this.preferredAccords = preferredAccords
            this.preferredBrands = preferredBrands
            this.reviewedPerfumeIds = reviewedPerfumeIds
            this.lastUpdated = LocalDateTime.now()
        }

        return memberPreferenceRepository.save(preferences)
    }

    /**
     * 선호하는 노트 계산
     *
     * @param memberId 회원 ID
     * @return 선호 노트 목록
     */
    private fun calculatePreferredNotes(memberId: Long): List<String> {
        // 높은 평점을 준 향수들에서 노트 추출 (평점 4 이상)
        val highRatedReviews =
            reviewRepository.findByMemberIdAndRatingValueGreaterThanEqual(memberId, 4)

        if (highRatedReviews.isEmpty()) {
            return emptyList()
        }

        val perfumeIds = highRatedReviews.map { it.perfume.id!! }

        // 노트 카운트 집계
        val noteCounts = mutableMapOf<String, Int>()

        perfumeIds.forEach { perfumeId ->
            val perfume = perfumeRepository.findById(perfumeId).orElse(null) ?: return@forEach

            // 향수의 노트 정보 조회
            val notes = perfume.getNotes()

            notes.forEach { perfumeNote ->
                val noteName = perfumeNote.note.name
                noteCounts[noteName] = (noteCounts[noteName] ?: 0) + 1
            }
        }

        // 카운트 기준으로 정렬하여 상위 노트 반환 (최대 10개)
        return noteCounts.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }
    }

    /**
     * 선호하는 어코드 계산
     *
     * @param memberId 회원 ID
     * @return 선호 어코드 목록
     */
    private fun calculatePreferredAccords(memberId: Long): List<String> {
        // 높은 평점을 준 향수들에서 어코드 추출 (평점 4 이상)
        val highRatedReviews =
            reviewRepository.findByMemberIdAndRatingValueGreaterThanEqual(memberId, 4)

        if (highRatedReviews.isEmpty()) {
            return emptyList()
        }

        val perfumeIds = highRatedReviews.map { it.perfume.id!! }

        // 어코드 카운트 집계
        val accordCounts = mutableMapOf<String, Int>()

        perfumeIds.forEach { perfumeId ->
            val perfume = perfumeRepository.findById(perfumeId).orElse(null) ?: return@forEach

            // 향수의 어코드 정보 조회
            val accords = perfume.getAccords()

            accords.forEach { perfumeAccord ->
                val accordName = perfumeAccord.accord.name
                accordCounts[accordName] = (accordCounts[accordName] ?: 0) + 1
            }
        }

        // 카운트 기준으로 정렬하여 상위 어코드 반환 (최대 10개)
        return accordCounts.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }
    }

    /**
     * 선호하는 브랜드 계산
     *
     * @param memberId 회원 ID
     * @return 선호 브랜드 목록
     */
    private fun calculatePreferredBrands(memberId: Long): List<String> {
        // 높은 평점을 준 리뷰에서 브랜드 추출 (평점 4 이상)
        val highRatedReviews =
            reviewRepository.findByMemberIdAndRatingValueGreaterThanEqual(memberId, 4)

        if (highRatedReviews.isEmpty()) {
            return emptyList()
        }

        // 브랜드 카운트 집계
        val brandCounts = mutableMapOf<String, Int>()

        highRatedReviews.forEach { review ->
            val brandName = review.perfume.brand.name
            brandCounts[brandName] = (brandCounts[brandName] ?: 0) + 1
        }

        // 카운트 기준으로 정렬하여 상위 브랜드 반환 (최대 5개)
        return brandCounts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }
    }

    /**
     * 리뷰한 향수 ID 목록 조회
     *
     * @param memberId 회원 ID
     * @return 리뷰한 향수 ID 목록
     */
    private fun getReviewedPerfumeIds(memberId: Long): List<Long> {
        return reviewRepository.findByMemberId(memberId)
            .map { it.perfume.id!! }
    }

    /**
     * 리뷰 생성 이벤트 처리
     */
    @Async
    @TransactionalEventListener
    fun handleReviewCreatedEvent(event: ReviewCreatedEvent) {
        updateMemberPreferences(event.memberId)
    }

    /**
     * 리뷰 업데이트 이벤트 처리
     */
    @Async
    @TransactionalEventListener
    fun handleReviewUpdatedEvent(event: ReviewUpdatedEvent) {
        updateMemberPreferences(event.memberId)
    }

    /**
     * 리뷰 삭제 이벤트 처리
     */
    @Async
    @TransactionalEventListener
    fun handleReviewDeletedEvent(event: ReviewDeletedEvent) {
        updateMemberPreferences(event.memberId)
    }

    /**
     * 투표 생성 이벤트 처리
     */
    @Async
    @TransactionalEventListener
    fun handleVoteCreatedEvent(event: VoteCreatedEvent) {
        updateMemberPreferences(event.memberId)
    }

    /**
     * 투표 업데이트 이벤트 처리
     */
    @Async
    @TransactionalEventListener
    fun handleVoteUpdatedEvent(event: VoteUpdatedEvent) {
        updateMemberPreferences(event.memberId)
    }

    /**
     * 회원 선호도 업데이트
     *
     * @param memberId 회원 ID
     */
    @CacheEvict(cacheNames = ["memberPreferences"], key = "#memberId")
    @Transactional
    fun updateMemberPreferences(memberId: Long) {
        calculateAndSaveMemberPreferences(memberId)
    }

    /**
     * 배치 작업: 모든 회원의 선호도 업데이트
     */
    @Async
    @Transactional
    fun updateAllMemberPreferences() {
        val members = memberRepository.findAll()

        members.forEach { member ->
            try {
                updateMemberPreferences(member.id!!)
            } catch (e: Exception) {
                // 로깅 처리
                println("Failed to update preferences for member ${member.id}: ${e.message}")
            }
        }
    }
}