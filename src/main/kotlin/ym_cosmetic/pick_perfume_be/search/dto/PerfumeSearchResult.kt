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
                id = document.id,
                name = document.name,
                brandName = document.brandName,
                imageUrl = document.imageUrl,
                averageRating = document.averageRating.toFloat(),
                reviewCount = document.reviewCount,
                topNotes = document.notes.filter { it.type == "TOP" }
                    .map { it.name },
                mainAccords = document.accords.map { it.name }
            )
        }
    }
}

