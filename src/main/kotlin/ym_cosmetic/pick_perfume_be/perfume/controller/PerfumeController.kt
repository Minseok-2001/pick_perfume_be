package ym_cosmetic.pick_perfume_be.perfume.controller

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
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeUpdateRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
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

    @GetMapping("/{id}")
    fun getPerfumeById(
        @PathVariable id: Long,
        @CurrentMember @OptionalAuth member: Member?
    ): ApiResponse<PerfumeResponse> {
        return ApiResponse.success(perfumeService.findPerfumeById(id, member))
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
}