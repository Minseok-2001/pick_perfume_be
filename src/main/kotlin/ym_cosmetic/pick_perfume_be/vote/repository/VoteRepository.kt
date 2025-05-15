package ym_cosmetic.pick_perfume_be.vote.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vote v WHERE v.member.id = :memberId AND v.perfume.id = :perfumeId AND v.category = :category")
    fun findByMemberIdAndPerfumeIdAndCategoryForUpdate(
        memberId: Long,
        perfumeId: Long,
        category: VoteCategory
    ): Vote?
}