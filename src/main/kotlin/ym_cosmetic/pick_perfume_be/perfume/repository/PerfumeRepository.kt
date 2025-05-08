package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.jpa.repository.JpaRepository
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume


interface PerfumeRepository : JpaRepository<Perfume, Long>