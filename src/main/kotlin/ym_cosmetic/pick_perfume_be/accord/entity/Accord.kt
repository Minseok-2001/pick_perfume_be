package ym_cosmetic.pick_perfume_be.accord.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity

@Entity
@Table(name = "accord")
class Accord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column
    var description: String? = null,

    @Column
    var color: String? = null,
    ) : BaseTimeEntity() {
    companion object {
        fun create(
            name: String,
            description: String? = null,
            color: String? = null,
        ): Accord {
            return Accord(
                name = name,
                description = description,
                color = color,
            )
        }
    }
}