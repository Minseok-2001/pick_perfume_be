package ym_cosmetic.pick_perfume_be.vote.dto.response

import ym_cosmetic.pick_perfume_be.vote.entity.Vote
import ym_cosmetic.pick_perfume_be.vote.vo.VoteCategory
import java.time.LocalDateTime

data class VoteResponse(
    val id: Long,

    val memberId: Long,

    val perfumeId: Long,

    val perfumeName: String,

    val category: VoteCategory,
    
    val categoryDisplayName: String,

    val value: String,

    val valueDisplayName: String,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(vote: Vote): VoteResponse {
            return VoteResponse(
                id = vote.id!!,
                memberId = vote.member.id!!,
                perfumeId = vote.perfume.id!!,
                perfumeName = vote.perfume.name,
                category = vote.category,
                categoryDisplayName = vote.category.displayName,
                value = vote.value,
                valueDisplayName = vote.category.getDisplayForValue(vote.value),
                createdAt = vote.createdAt,
                updatedAt = vote.updatedAt
            )
        }
    }
}