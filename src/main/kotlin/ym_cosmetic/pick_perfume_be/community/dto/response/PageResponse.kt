package ym_cosmetic.pick_perfume_be.community.dto.response

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
) {
    companion object {
        fun <T> from(page: Page<T>): PageResponse<T> {
            return PageResponse(
                content = page.content,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                page = page.number,
                size = page.size,
                first = page.isFirst,
                last = page.isLast,
                empty = page.isEmpty
            )
        }

        fun <T> empty(): PageResponse<T> {
            return PageResponse(
                content = emptyList(),
                totalElements = 0,
                totalPages = 0,
                page = 0,
                size = 0,
                first = true,
                last = true,
                empty = true
            )
        }
    }
} 