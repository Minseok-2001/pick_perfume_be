package ym_cosmetic.pick_perfume_be.designer.controller

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.designer.dto.response.DesignerResponse
import ym_cosmetic.pick_perfume_be.designer.service.DesignerService
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.security.CurrentMember
import ym_cosmetic.pick_perfume_be.security.OptionalAuth

@RestController
@RequestMapping("/api/designers")
class DesignerController(
    private val designerService: DesignerService
) {
    @GetMapping("/popular")
    fun getPopularDesigners(
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiResponse<List<DesignerResponse>> {
        val designers = designerService.findPopularDesigners(limit)
        return ApiResponse.success(designers)
    }
    
    @GetMapping("/{id}/perfumes")
    fun getPerfumesByDesigner(
        @PathVariable id: Long,
        @PageableDefault(size = 20) pageable: Pageable,
        @CurrentMember @OptionalAuth member: Member?
    ): ApiResponse<Page<PerfumeSummaryResponse>> {
        val perfumes = designerService.findPerfumesByDesigner(id, pageable, member)
        return ApiResponse.success(perfumes)
    }
} 