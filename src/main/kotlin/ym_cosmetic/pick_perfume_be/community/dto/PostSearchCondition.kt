package ym_cosmetic.pick_perfume_be.community.dto

import java.time.LocalDateTime

data class PostSearchCondition(
    val keyword: String? = null,
    val boardId: Long? = null,
    val memberId: Long? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val hasPerfumeEmbed: Boolean? = null,
    val perfumeId: Long? = null
) 