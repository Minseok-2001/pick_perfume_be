package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.accord.entity.Accord
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity

@Entity
@Table(name = "perfume_accord")
class PerfumeAccord private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "perfume_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    val perfume: Perfume,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "accord_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    val accord: Accord,

    @Column
    var position: Int? = null,
) : BaseTimeEntity() {

    companion object {
        fun create(
            perfume: Perfume,
            accord: Accord,
            position: Int? = null
        ): PerfumeAccord {
            return PerfumeAccord(
                perfume = perfume,
                accord = accord,
                position = position
            )
        }
    }
}