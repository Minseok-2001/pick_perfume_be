package ym_cosmetic.pick_perfume_be.note.repository

import org.springframework.data.jpa.repository.JpaRepository
import ym_cosmetic.pick_perfume_be.note.entity.Note


interface NoteRepository : JpaRepository<Note, Long> {
    fun findByNameIgnoreCase(name: String): Note?
    fun findByName(name: String): Note?

}