package ym_cosmetic.pick_perfume_be.designer.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.designer.dto.response.DesignerResponse
import ym_cosmetic.pick_perfume_be.designer.repository.DesignerRepository
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeDesignerRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository

@Service
class DesignerService(
    private val designerRepository: DesignerRepository,
    private val perfumeDesignerRepository: PerfumeDesignerRepository,
    private val perfumeRepository: PerfumeRepository
) {
    @Transactional(readOnly = true)
    fun findPopularDesigners(limit: Int = 10): List<DesignerResponse> {
        return designerRepository.findMostProlificDesigners(limit)
            .map { DesignerResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun findPerfumesByDesigner(designerId: Long, pageable: Pageable): Page<PerfumeSummaryResponse> {
        designerRepository.findById(designerId)
            .orElseThrow { EntityNotFoundException("Designer not found with id: $designerId") }

        val perfumeIds = perfumeDesignerRepository.findPerfumeIdsByDesignerId(designerId)
        return perfumeRepository.findByIdIn(perfumeIds, pageable)
            .map { PerfumeSummaryResponse.from(it) }
    }

}