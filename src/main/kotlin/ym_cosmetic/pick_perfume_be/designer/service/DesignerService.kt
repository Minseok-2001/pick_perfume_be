package ym_cosmetic.pick_perfume_be.designer.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.designer.dto.response.DesignerResponse
import ym_cosmetic.pick_perfume_be.designer.repository.DesignerRepository
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeDesignerRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeLikeRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository

@Service
class DesignerService(
    private val designerRepository: DesignerRepository,
    private val perfumeDesignerRepository: PerfumeDesignerRepository,
    private val perfumeRepository: PerfumeRepository,
    private val perfumeLikeRepository: PerfumeLikeRepository
) {
    @Transactional(readOnly = true)
    fun findPopularDesigners(limit: Int = 10): List<DesignerResponse> {
        return designerRepository.findMostProlificDesigners(limit)
            .map { DesignerResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun findPerfumesByDesigner(designerId: Long, pageable: Pageable, member: Member?): Page<PerfumeSummaryResponse> {
        designerRepository.findById(designerId)
            .orElseThrow { EntityNotFoundException("Designer not found with id: $designerId") }

        val perfumeIds = perfumeDesignerRepository.findPerfumeIdsByDesignerId(designerId)
        val perfumePage = perfumeRepository.findByIdIn(perfumeIds, pageable)
        
        // 회원이 좋아요한 향수 ID 목록 조회
        val likedPerfumeIds = getLikedPerfumeIdsByMember(member)
        
        return perfumePage.map { perfume ->
            PerfumeSummaryResponse.from(
                perfume = perfume,
                isLiked = likedPerfumeIds.contains(perfume.id)
            )
        }
    }
    
    private fun getLikedPerfumeIdsByMember(member: Member?): Set<Long> {
        if (member == null || member.id == null) {
            return emptySet()
        }
        
        return perfumeLikeRepository.findPerfumeIdsByMemberId(member.id!!)
    }
}