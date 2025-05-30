package ym_cosmetic.pick_perfume_be.perfume.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.enums.MemberRole
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeCreateRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeFilterRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeUpdateRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumePageResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryStats
import ym_cosmetic.pick_perfume_be.perfume.service.PerfumeService
import ym_cosmetic.pick_perfume_be.security.CurrentMember
import ym_cosmetic.pick_perfume_be.security.OptionalAuth
import ym_cosmetic.pick_perfume_be.security.RequireRole

@RestController
@RequestMapping("/api/perfumes")
class PerfumeController(
    private val perfumeService: PerfumeService
) {
    @GetMapping
    fun getAllPerfumes(
        @PageableDefault(size = 20) pageable: Pageable,
        @CurrentMember @OptionalAuth member: Member?
    ): ApiResponse<Page<PerfumeSummaryResponse>> {
        val perfumes = perfumeService.findAllApprovedPerfumes(pageable, member)
        return ApiResponse.success(perfumes)
    }

    @GetMapping("/filtered")
    fun getFilteredPerfumes(
        @ModelAttribute filter: PerfumeFilterRequest?,
        @RequestParam(defaultValue = "false") includeStats: Boolean,
        @PageableDefault(size = 20) pageable: Pageable,
        @CurrentMember @OptionalAuth member: Member?
    ): ApiResponse<PerfumePageResponse> {
        val result =
            perfumeService.findAllPerfumesWithFilter(filter, pageable, member, includeStats)
        return ApiResponse.success(result)
    }

    @GetMapping("/stats")
    fun getPerfumeStats(): ApiResponse<PerfumeSummaryStats> {
        val stats = perfumeService.getPerfumeStatistics()
        return ApiResponse.success(stats)
    }


    /**
     * 향수 상세정보 조회 (조회수 증가)
     */
    @GetMapping("/{id}")
    fun getPerfumeById(
        @PathVariable id: Long,
        @CurrentMember @OptionalAuth member: Member?,
        request: HttpServletRequest
    ): ApiResponse<PerfumeResponse> {
        return ApiResponse.success(
            perfumeService.findPerfumeByIdAndIncreaseViewCount(
                id,
                member,
                request
            )
        )
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole(MemberRole.ADMIN)
    fun createPerfume(
        @RequestBody request: PerfumeCreateRequest,
        @CurrentMember member: Member
    ): ApiResponse<PerfumeResponse> {
        return ApiResponse.success(perfumeService.createPerfume(request, member.id))
    }

    @PutMapping("/{id}")
    @RequireRole(MemberRole.ADMIN)
    fun updatePerfume(
        @PathVariable id: Long,
        @RequestBody request: PerfumeUpdateRequest
    ): ApiResponse<PerfumeResponse> {
        return ApiResponse.success(perfumeService.updatePerfume(id, request))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequireRole(MemberRole.ADMIN)
    fun deletePerfume(@PathVariable id: Long) {
        perfumeService.deletePerfume(id)
    }

    @PostMapping("/{id}/approve")
    @RequireRole(MemberRole.ADMIN)
    fun approvePerfume(@PathVariable id: Long): ApiResponse<PerfumeResponse> {
        return ApiResponse.success(perfumeService.approvePerfume(id))
    }

    @PostMapping("/{id}/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @RequireRole(MemberRole.ADMIN)
    fun uploadPerfumeImage(
        @PathVariable id: Long,
        @RequestParam("file") file: MultipartFile
    ): ApiResponse<String> {
        val imageUrl = perfumeService.uploadPerfumeImage(id, file)
        return ApiResponse.success(imageUrl)
    }

    @PostMapping("/{id}/like")
    fun likePerfume(
        @PathVariable id: Long,
        @CurrentMember member: Member
    ): ApiResponse<Boolean> {
        return ApiResponse.success(perfumeService.likePerfume(id, member))
    }

    @DeleteMapping("/{id}/like")
    fun unlikePerfume(
        @PathVariable id: Long,
        @CurrentMember member: Member
    ): ApiResponse<Boolean> {
        return ApiResponse.success(perfumeService.unlikePerfume(id, member))
    }
    
    /**
     * 사용자가 좋아요한 향수 목록 조회
     */
    @GetMapping("/likes")
    fun getLikedPerfumes(
        @PageableDefault(size = 20) pageable: Pageable,
        @CurrentMember member: Member
    ): ApiResponse<Page<PerfumeSummaryResponse>> {
        return ApiResponse.success(perfumeService.findLikedPerfumes(member, pageable))
    }
    
    /**
     * 사용자가 조회한 향수 목록 조회
     */
    @GetMapping("/views")
    fun getViewedPerfumes(
        @PageableDefault(size = 20) pageable: Pageable,
        @CurrentMember member: Member
    ): ApiResponse<Page<PerfumeSummaryResponse>> {
        return ApiResponse.success(perfumeService.findViewedPerfumes(member, pageable))
    }
}