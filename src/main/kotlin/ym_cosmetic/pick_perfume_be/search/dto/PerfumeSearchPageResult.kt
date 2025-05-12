package ym_cosmetic.pick_perfume_be.search.dto

import kotlin.math.ceil

data class PerfumeSearchPageResult(
    val content: List<PerfumeSearchResult>,
    val totalCount: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int
) {
    companion object {
        fun of(content: List<PerfumeSearchResult>, totalCount: Long, page: Int, size: Int): PerfumeSearchPageResult {
            val totalPages = if (size > 0) ceil(totalCount.toDouble() / size).toInt() else 0
            return PerfumeSearchPageResult(
                content = content,
                totalCount = totalCount,
                page = page,
                size = size,
                totalPages = totalPages
            )
        }
    }
} 