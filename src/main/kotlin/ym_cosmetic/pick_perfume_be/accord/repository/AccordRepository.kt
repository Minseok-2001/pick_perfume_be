package ym_cosmetic.pick_perfume_be.accord.repository

import org.springframework.data.jpa.repository.JpaRepository
import ym_cosmetic.pick_perfume_be.accord.entity.Accord

interface AccordRepository : JpaRepository<Accord, Long> {
    fun findByNameIgnoreCase(name: String): Accord?
    fun findByName(name: String): Accord?
}