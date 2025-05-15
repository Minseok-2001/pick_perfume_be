package ym_cosmetic.pick_perfume_be.vote.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.common.exception.InvalidRequestException
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.vote.entity.PerfumeVoteStatistics
import ym_cosmetic.pick_perfume_be.vote.entity.Vote
import ym_cosmetic.pick_perfume_be.vote.event.VoteCreatedEvent
import ym_cosmetic.pick_perfume_be.vote.event.VoteUpdatedEvent
import ym_cosmetic.pick_perfume_be.vote.repository.PerfumeVoteStatisticsRepository
import ym_cosmetic.pick_perfume_be.vote.repository.VoteRepository
import ym_cosmetic.pick_perfume_be.vote.vo.VoteCategory
import ym_cosmetic.pick_perfume_be.vote.vo.VoteResult
import java.time.LocalDateTime

@Service
class VoteService(
    private val voteRepository: VoteRepository,
    private val memberRepository: MemberRepository,
    private val perfumeRepository: PerfumeRepository,
    private val perfumeVoteStatisticsRepository: PerfumeVoteStatisticsRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    companion object {
        private val logger = LoggerFactory.getLogger(VoteService::class.java)
    }

    @Transactional(readOnly = true)
    fun getVoteStatistics(perfumeId: Long): Map<VoteCategory, VoteResult> {
        // 캐시된 통계가 있는지 확인
        val cachedStats = perfumeVoteStatisticsRepository.findByPerfumeId(perfumeId)

        if (cachedStats != null &&
            cachedStats.lastUpdated.isAfter(LocalDateTime.now().minusHours(1))
        ) {
            // 최근 1시간 이내 업데이트된 통계가 있으면 그대로 사용
            return convertToVoteResults(cachedStats.statistics)
        }

        // 캐시된 통계가 없거나 오래된 경우 새로 계산
        // 향수 존재 확인
        perfumeRepository.findById(perfumeId)
            .orElseThrow { EntityNotFoundException("Perfume not found with id: $perfumeId") }

        // 모든 투표 조회
        val votes = voteRepository.findByPerfumeId(perfumeId)

        // 카테고리별 투표 결과 계산
        return VoteCategory.values().associateWith { category ->
            VoteResult.fromVotes(category, votes.filter { it.category == category })
        }.also {
            // 새로 계산한 통계를 비동기적으로 캐싱
            updatePerfumeVoteStatisticsAsync(perfumeId, it)
        }
    }

    @Transactional
    fun createVote(memberId: Long, perfumeId: Long, category: VoteCategory, value: String): Vote {
        // 요청 값 유효성 검증
        if (!category.isValidValue(value)) {
            throw InvalidRequestException("Invalid vote value '$value' for category ${category.name}. Allowed values: ${category.getAllowedValues()}")
        }

        // 회원 조회
        val member = memberRepository.findById(memberId)
            .orElseThrow { EntityNotFoundException("Member not found with id: $memberId") }

        // 향수 조회
        val perfume = perfumeRepository.findById(perfumeId)
            .orElseThrow { EntityNotFoundException("Perfume not found with id: $perfumeId") }

        // 이미 해당 카테고리에 투표했는지 확인 - 락 획득을 위해 for update 사용
        val existingVote = voteRepository.findByMemberIdAndPerfumeIdAndCategoryForUpdate(
            memberId,
            perfumeId,
            category
        )
        val statistics = getVoteStatistics(perfumeId)

        if (existingVote != null) {
            // 이미 투표한 경우, 값 업데이트
            existingVote.updateValue(value)
            val updatedVote = voteRepository.save(existingVote)

            // 이벤트 발행
            eventPublisher.publishEvent(VoteUpdatedEvent(memberId, perfumeId, category, value))

            // 투표 통계 업데이트 - 비동기로 처리
            updatePerfumeVoteStatisticsAsync(perfumeId, statistics)

            return updatedVote
        }

        // 새 투표 생성
        val vote = Vote(
            member = member,
            perfume = perfume,
            category = category,
            value = value
        )

        val savedVote = voteRepository.save(vote)

        // 이벤트 발행
        eventPublisher.publishEvent(VoteCreatedEvent(memberId, perfumeId, category.name, value))

        // 투표 통계 업데이트 - 비동기로 처리
        updatePerfumeVoteStatisticsAsync(perfumeId, statistics)

        return savedVote
    }

    @Transactional(readOnly = true)
    fun getVoteCountsByCategory(perfumeId: Long, category: VoteCategory): Map<String, Int> {
        val votes = voteRepository.findByPerfumeIdAndCategory(perfumeId, category)

        // 카테고리의 모든 가능한 값에 대해 초기 카운트 0으로 설정
        val counts = category.getAllowedValues().associateWith { 0 }.toMutableMap()

        // 실제 투표 수 집계
        votes.forEach { vote ->
            counts[vote.value] = (counts[vote.value] ?: 0) + 1
        }

        return counts
    }

    @Transactional
    fun updateVote(voteId: Long, newValue: String): Vote {
        // 투표 조회
        val vote = voteRepository.findById(voteId)
            .orElseThrow { EntityNotFoundException("Vote not found with id: $voteId") }

        // 값 유효성 검증
        if (!vote.category.isValidValue(newValue)) {
            throw InvalidRequestException(
                "Invalid vote value '$newValue' for category ${vote.category.name}. " +
                        "Allowed values: ${vote.category.getAllowedValues()}"
            )
        }

        // 값이 같으면 바로 반환 (불필요한 업데이트 방지)
        if (vote.value == newValue) {
            return vote
        }

        // 투표 값 업데이트
        vote.updateValue(newValue)

        // 이벤트 발행
        eventPublisher.publishEvent(
            VoteUpdatedEvent(
                vote.member.id!!,
                vote.perfume.id!!,
                vote.category,
                newValue
            )
        )

        // 투표 통계 업데이트
        updatePerfumeVoteStatistics(vote.perfume.id!!)

        return vote
    }

    /**
     * 회원이 특정 향수의 특정 카테고리에 투표했는지 확인
     */
    @Transactional(readOnly = true)
    fun hasVoted(memberId: Long, perfumeId: Long, category: VoteCategory): Boolean {
        return voteRepository.existsByMemberIdAndPerfumeIdAndCategory(memberId, perfumeId, category)
    }

    /**
     * 회원의 특정 향수에 대한 투표 조회
     */
    @Transactional(readOnly = true)
    fun getVotesByMemberAndPerfume(memberId: Long, perfumeId: Long): List<Vote> {
        return voteRepository.findByMemberIdAndPerfumeId(memberId, perfumeId)
    }

    /**
     * 투표 삭제
     */
    @Transactional
    fun deleteVote(voteId: Long) {
        val vote = voteRepository.findById(voteId)
            .orElseThrow { EntityNotFoundException("Vote not found with id: $voteId") }

        val perfumeId = vote.perfume.id!!

        voteRepository.delete(vote)

        // 투표 통계 업데이트
        updatePerfumeVoteStatistics(perfumeId)
    }

    /**
     * 향수의 투표 통계 업데이트
     * 투표가 추가/수정/삭제될 때마다 호출됨
     */
    @Transactional
    fun updatePerfumeVoteStatistics(perfumeId: Long) {
        try {
            val perfume = perfumeRepository.findById(perfumeId)
                .orElseThrow { EntityNotFoundException("Perfume not found with id: $perfumeId") }

            val votes = voteRepository.findByPerfumeId(perfumeId)

            // 카테고리별 투표 결과 집계
            val statistics = VoteCategory.values().associate { category ->
                category.name to votes.filter { it.category == category }
                    .groupBy { it.value }
                    .mapValues { it.value.size }
            }

            // 통계 저장 또는 업데이트 - 락 획득
            val stats = perfumeVoteStatisticsRepository.findByPerfumeIdForUpdate(perfumeId)
                ?: PerfumeVoteStatistics(
                    perfumeId = perfumeId,
                    perfume = perfume,
                    statistics = mutableMapOf(),
                    lastUpdated = LocalDateTime.now()
                )

            // 통계 업데이트
            stats.statistics = statistics
            stats.lastUpdated = LocalDateTime.now()

            perfumeVoteStatisticsRepository.save(stats)
        } catch (e: Exception) {
            logger.error("Failed to update perfume vote statistics: ${e.message}", e)
            // 실패해도 투표 자체는 성공하도록 예외를 던지지 않음
        }
    }

    private fun convertToVoteResults(statistics: Map<String, Map<String, Int>>): Map<VoteCategory, VoteResult> {
        return VoteCategory.values().associateWith { category ->
            val counts = statistics[category.name] ?: emptyMap()
            val totalVotes = counts.values.sum()
            val topValue = if (counts.isEmpty()) null
            else counts.maxByOrNull { it.value }?.key

            VoteResult(
                category = category,
                counts = counts,
                totalVotes = totalVotes,
                topValue = topValue
            )
        }
    }


    private fun updatePerfumeVoteStatisticsAsync(
        perfumeId: Long,
        statistics: Map<VoteCategory, VoteResult>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val perfume = perfumeRepository.findById(perfumeId).orElse(null) ?: return@launch

                val statisticsMap = statistics.entries.associate { (category, result) ->
                    category.name to result.counts
                }

                val stats = perfumeVoteStatisticsRepository.findByPerfumeId(perfumeId)
                    ?: PerfumeVoteStatistics(
                        perfumeId = perfumeId,
                        perfume = perfume,
                        statistics = statisticsMap,
                        lastUpdated = LocalDateTime.now()
                    )
                if (stats.perfumeId != null) {
                    stats.statistics = statisticsMap
                    stats.lastUpdated = LocalDateTime.now()
                }

                perfumeVoteStatisticsRepository.save(stats)
            } catch (e: Exception) {
                logger.error("Failed to update perfume vote statistics: ${e.message}", e)
            }
        }
    }


}