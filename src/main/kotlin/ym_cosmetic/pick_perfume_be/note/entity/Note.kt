package ym_cosmetic.pick_perfume_be.note.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl

@Entity
@Table(name = "note")
class Note(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column
    var content: String? = null,

    @Embedded
    var image: ImageUrl? = null,
) : BaseTimeEntity() {
    companion object {
        fun create(
            name: String,
            content: String? = null,
            image: ImageUrl? = null
        ): Note {
            return Note(
                name = name,
                content = content,
                image = image
            )
        }
    }
}