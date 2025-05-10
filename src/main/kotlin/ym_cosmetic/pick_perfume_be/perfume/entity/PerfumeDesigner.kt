package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.designer.entity.Designer
import ym_cosmetic.pick_perfume_be.perfume.enums.DesignerRole

@Entity
@Table(name = "perfume_designer")
class PerfumeDesigner private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfume_id", nullable = false)
    val perfume: Perfume,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id", nullable = false)
    val designer: Designer,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: DesignerRole,

    @Column(length = 500)
    var description: String? = null
) : BaseTimeEntity() {

    companion object {
        fun create(
            perfume: Perfume,
            designer: Designer,
            role: DesignerRole,
            description: String? = null
        ): PerfumeDesigner {
            return PerfumeDesigner(
                perfume = perfume,
                designer = designer,
                role = role,
                description = description
            )
        }
    }
    
    fun updateDescription(description: String?): PerfumeDesigner {
        this.description = description
        return this
    }
}

