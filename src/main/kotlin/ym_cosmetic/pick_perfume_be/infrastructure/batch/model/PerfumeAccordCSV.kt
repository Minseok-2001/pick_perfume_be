package ym_cosmetic.pick_perfume_be.infrastructure.batch.model

data class PerfumeAccordCSV(
    val id: Long,
    val perfumeId: Long,
    val accordName: String,
    val position: Int
) 