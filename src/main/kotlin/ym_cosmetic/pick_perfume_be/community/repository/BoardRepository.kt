package ym_cosmetic.pick_perfume_be.community.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.community.entity.Board
import java.util.*

@Repository
interface BoardRepository : JpaRepository<Board, Long> {
    
    fun findByName(name: String): Optional<Board>
    
    @Query("SELECT b FROM Board b WHERE b.isActive = true ORDER BY b.displayOrder ASC")
    fun findAllActiveOrderByDisplayOrder(): List<Board>
} 