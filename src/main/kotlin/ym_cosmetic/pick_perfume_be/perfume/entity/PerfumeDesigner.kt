package ym_cosmetic.pick_perfume_be.perfume.entity


import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.designer.entity.Designer
import ym_cosmetic.pick_perfume_be.perfume.enums.DesignerRole

@Entity
@Table(
    name = "perfume_designer",
    uniqueConstraints = [UniqueConstraint(columnNames = ["perfume_id", "designer_id", "role"])]
)
class PerfumeDesigner(
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
        name = "designer_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    var designer: Designer,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: DesignerRole,

    @Column
    var description: String? = null,

    ) : BaseTimeEntity()

