package ym_cosmetic.pick_perfume_be.brand.repository

import org.springframework.data.jpa.repository.JpaRepository
import ym_cosmetic.pick_perfume_be.brand.entity.Brand

interface BrandRepository : JpaRepository<Brand, Long> {
    fun findByCountryCode(countryCode: String): List<Brand>

    fun findByNameIgnoreCase(name: String): Brand?
}