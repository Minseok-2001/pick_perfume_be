package ym_cosmetic.pick_perfume_be.designer.repository

import ym_cosmetic.pick_perfume_be.designer.entity.Designer

interface DesignerRepositoryCustom {
    fun findMostProlificDesigners(limit: Int): List<Designer>
}