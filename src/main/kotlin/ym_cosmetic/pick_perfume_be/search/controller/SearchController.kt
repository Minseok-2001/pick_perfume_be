package ym_cosmetic.pick_perfume_be.search.controller

import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchCriteria
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchPageResult
import ym_cosmetic.pick_perfume_be.search.service.PerfumeSearchService

@RestController
@RequestMapping("/api/search")
class SearchController(
    private val perfumeSearchService: PerfumeSearchService
) {
    @GetMapping("/perfumes")
    fun searchPerfumes(
        @RequestParam keyword: String?,
        @RequestParam brandName: String? = null,
        @RequestParam noteType: NoteType? = null,
        @RequestParam note: String? = null,
        @RequestParam accord: String? = null,
        @RequestParam fromYear: Int? = null,
        @RequestParam toYear: Int? = null,
        @RequestParam minRating: Double? = null,
        @RequestParam maxRating: Double? = null,
        @RequestParam sortBy: String = "relevance",
        @RequestParam page: Int = 0,
        @RequestParam size: Int = 20
    ): ApiResponse<PerfumeSearchPageResult> {
        val pageable = PageRequest.of(page, size)

        val criteria = PerfumeSearchCriteria(
            keyword = keyword,
            brandName = brandName,
            noteType = noteType,
            note = note,
            accord = accord,
            fromYear = fromYear,
            toYear = toYear,
            minRating = minRating,
            maxRating = maxRating,
            sortBy = sortBy,
            pageable = pageable
        )

        val results = perfumeSearchService.searchPerfumes(criteria)
        return ApiResponse.success(results)
    }
}

