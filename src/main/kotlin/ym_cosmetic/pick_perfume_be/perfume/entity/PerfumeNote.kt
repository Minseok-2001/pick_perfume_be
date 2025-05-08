package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import ym_cosmetic.pick_perfume_be.note.entity.Note
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType
import java.time.LocalDateTime

@Entity
@Table(name = "perfume_notes")
class PerfumeNote(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "perfume_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    var perfume: Perfume,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "note_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    var note: Note,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NoteType,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)