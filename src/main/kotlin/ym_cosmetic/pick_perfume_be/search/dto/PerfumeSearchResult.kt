package ym_cosmetic.pick_perfume_be.search.dto

import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument

data class PerfumeSearchResult(
    val id: Long,
    val name: String,
    val brandName: String,
    val imageUrl: String?,
    val averageRating: Float,
    val reviewCount: Int,
    val topNotes: List<String>,
    val mainAccords: List<String>
) {
    companion object {
        fun fromDocument(document: PerfumeDocument): PerfumeSearchResult {
            return PerfumeSearchResult(
                id = document.id.toLong(),
                name = document.name,
                brandName = document.brandName,
                imageUrl = null, // 이 정보는 문서에 없으므로 필요하면 추가
                averageRating = document.averageRating,
                reviewCount = document.reviewCount,
                topNotes = document.notesByType
                    .find { it.type == "TOP" }
                    ?.notes ?: emptyList(),
                mainAccords = document.accords.take(3)
            )
        }
    }
}

