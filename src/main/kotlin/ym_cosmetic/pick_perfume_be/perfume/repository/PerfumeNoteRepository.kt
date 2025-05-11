package ym_cosmetic.pick_perfume_be.perfume.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeNote
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType

interface PerfumeNoteRepository : JpaRepository<PerfumeNote, Long> {
    fun findByPerfumeId(perfumeId: Long): List<PerfumeNote>

    fun findByPerfumeIdAndType(perfumeId: Long, type: NoteType): List<PerfumeNote>

    fun findByPerfumeIdAndNoteId(perfumeId: Long, noteId: Long): PerfumeNote?

    @Modifying
    @Query("DELETE FROM PerfumeNote pn WHERE pn.perfume.id = :perfumeId")
    fun deleteByPerfumeId(perfumeId: Long)

    @Modifying
    @Query("DELETE FROM PerfumeNote pn WHERE pn.perfume.id = :perfumeId AND pn.type = :type")
    fun deleteByPerfumeIdAndType(perfumeId: Long, type: NoteType)
}