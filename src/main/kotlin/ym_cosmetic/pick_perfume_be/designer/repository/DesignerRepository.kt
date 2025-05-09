package ym_cosmetic.pick_perfume_be.designer.repository

import org.springframework.data.jpa.repository.JpaRepository
import ym_cosmetic.pick_perfume_be.designer.entity.Designer

interface DesignerRepository : JpaRepository<Designer, Long>, DesignerRepositoryCustom {
    fun findByNameIgnoreCase(name: String): Designer?
}