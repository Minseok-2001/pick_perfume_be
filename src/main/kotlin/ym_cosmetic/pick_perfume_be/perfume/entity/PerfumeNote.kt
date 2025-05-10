package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.note.entity.Note
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType

@Entity
@Table(name = "perfume_note")
class PerfumeNote private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfume_id", nullable = false)
    val perfume: Perfume,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    val note: Note,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NoteType
) : BaseTimeEntity() {

    companion object {
        fun create(
            perfume: Perfume,
            note: Note,
            type: NoteType
        ): PerfumeNote {
            return PerfumeNote(
                perfume = perfume,
                note = note,
                type = type
            )
        }
    }
}