package ym_cosmetic.pick_perfume_be.infrastructure.batch.model

data class PerfumeNoteCSV(
    val id: Long,
    val perfumeId: Long,
    val noteType: String,
    val noteName: String
) 