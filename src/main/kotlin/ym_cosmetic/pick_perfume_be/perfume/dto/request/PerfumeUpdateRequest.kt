package ym_cosmetic.pick_perfume_be.perfume.dto.request

import ym_cosmetic.pick_perfume_be.perfume.vo.Concentration

data class PerfumeUpdateRequest(
    val name: String,
    val brandName: String,
    val description: String?,
    val releaseYear: Int?,
    val concentration: Concentration?,
    val imageUrl: String?,
    val topNotes: List<String> = emptyList(),
    val middleNotes: List<String> = emptyList(),
    val baseNotes: List<String> = emptyList(),
    val accords: List<String> = emptyList(),
    val designers: List<PerfumeDesignerRequest> = emptyList()
)