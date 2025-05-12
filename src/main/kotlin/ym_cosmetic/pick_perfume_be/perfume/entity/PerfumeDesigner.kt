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
    @JoinColumn(
        name = "perfume_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    val perfume: Perfume,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "designer_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    val designer: Designer,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val designerRole: DesignerRole,

    @Column(length = 500)
    var content: String? = null
) : BaseTimeEntity() {

    companion object {
        fun create(
            perfume: Perfume,
            designer: Designer,
            role: DesignerRole,
            content: String? = null
        ): PerfumeDesigner {
            return PerfumeDesigner(
                perfume = perfume,
                designer = designer,
                designerRole = role,
                content = content
            )
        }
    }

    fun updatecontent(content: String?): PerfumeDesigner {
        this.content = content
        return this
    }
}

