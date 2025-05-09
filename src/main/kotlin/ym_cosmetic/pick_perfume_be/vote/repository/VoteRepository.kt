package ym_cosmetic.pick_perfume_be.vote.repository

import org.springframework.data.jpa.repository.JpaRepository
import ym_cosmetic.pick_perfume_be.vote.entity.Vote
import ym_cosmetic.pick_perfume_be.vote.vo.VoteCategory

interface VoteRepository : JpaRepository<Vote, Long> {
    fun findByPerfumeId(perfumeId: Long): List<Vote>

    fun findByMemberIdAndPerfumeId(memberId: Long, perfumeId: Long): List<Vote>

    fun findByPerfumeIdAndCategory(
        perfumeId: Long,
        category: VoteCategory
    ): List<Vote>

    fun findByMemberIdAndPerfumeIdAndCategory(
        memberId: Long,
        perfumeId: Long,
        category: VoteCategory
    ): Vote?

    fun existsByMemberIdAndPerfumeIdAndCategory(
        memberId: Long,
        perfumeId: Long,
        category: VoteCategory
    ): Boolean

    fun countByPerfumeIdAndCategoryAndValue(
        perfumeId: Long,
        category: VoteCategory,
        value: String
    ): Int

    fun deleteByMemberIdAndPerfumeId(memberId: Long, perfumeId: Long)
}