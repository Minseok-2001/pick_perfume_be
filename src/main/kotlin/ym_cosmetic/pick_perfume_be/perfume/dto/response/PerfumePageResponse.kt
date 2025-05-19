package ym_cosmetic.pick_perfume_be.perfume.dto.response

import org.springframework.data.domain.Page

data class PerfumePageResponse(
    val perfumes: Page<PerfumeSummaryResponse>,
    val stats: PerfumeSummaryStats?
) 