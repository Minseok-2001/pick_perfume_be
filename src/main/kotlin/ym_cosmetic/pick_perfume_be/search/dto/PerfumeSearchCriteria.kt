package ym_cosmetic.pick_perfume_be.search.dto

import org.springframework.data.domain.Pageable
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType

data class PerfumeSearchCriteria(
    val keyword: String? = null,
    val brandName: String? = null,
    val note: String? = null,
    val noteType: NoteType? = null,
    val accord: String? = null,
    val fromYear: Int? = null,
    val toYear: Int? = null,
    val minRating: Double? = null,
    val maxRating: Double? = null,
    val season: String? = null,
    val gender: String? = null,
    val sortBy: String = "relevance",
    val pageable: Pageable,
    val limit: Int? = null
)