package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume


interface PerfumeRepository : JpaRepository<Perfume, Long>, PerfumeRepositoryCustom {
    fun findByIdIn(ids: List<Long>, pageable: Pageable): Page<Perfume>


}
