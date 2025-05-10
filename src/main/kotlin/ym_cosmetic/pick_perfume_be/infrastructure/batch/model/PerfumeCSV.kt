package ym_cosmetic.pick_perfume_be.infrastructure.batch.model

data class PerfumeCSV(
    val id: Long,
    val url: String,
    val title: String,
    val brandId: Long,
    val gender: String?,
    val ratingValue: Double?,
    val ratingCount: Int?,
    val year: Int?,
    val perfumer1: String?,
    val perfumer2: String?,
    val description: String?,
    val updatedAt: String?
    // embedding 관련 필드는 생략
) 