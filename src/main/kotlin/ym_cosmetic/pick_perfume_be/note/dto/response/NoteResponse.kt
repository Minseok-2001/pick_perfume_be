// note/dto/response/NoteResponse.kt
package ym_cosmetic.pick_perfume_be.note.dto.response

import ym_cosmetic.pick_perfume_be.note.entity.Note

data class NoteResponse(
    val id: Long,
    val name: String,
    val imageUrl: String?
) {
    companion object {
        fun from(note: Note): NoteResponse {
            return NoteResponse(
                id = note.id!!,
                name = note.name,
                imageUrl = note.image?.url
            )
        }
    }
}