package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAiImageVote

@Repository
interface PerfumeAiImageVoteRepository : JpaRepository<PerfumeAiImageVote, Long> {
    fun countByAiImageId(aiImageId: Long): Long
    fun findByAiImageIdAndMemberId(aiImageId: Long, memberId: Long): PerfumeAiImageVote?
    fun findByPerfumeIdAndMemberId(perfumeId: Long, memberId: Long): PerfumeAiImageVote?
    fun findByPerfumeIdAndIpAddress(perfumeId: Long, ipAddress: String): PerfumeAiImageVote?
    fun deleteByPerfumeId(perfumeId: Long)
}
