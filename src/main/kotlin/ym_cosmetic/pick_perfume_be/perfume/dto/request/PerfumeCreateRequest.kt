package ym_cosmetic.pick_perfume_be.perfume.dto.request

import ym_cosmetic.pick_perfume_be.perfume.vo.Concentration

data class PerfumeCreateRequest(
    val name: String,
    val brand: String,
    val description: String?,
    val releaseYear: Int?,
    val perfumer: String?,
    val concentration: Concentration?,
    val imageUrl: String?,
    val topNotes: List<String> = emptyList(),
    val middleNotes: List<String> = emptyList(),
    val baseNotes: List<String> = emptyList(),
    val accords: List<String> = emptyList()
)