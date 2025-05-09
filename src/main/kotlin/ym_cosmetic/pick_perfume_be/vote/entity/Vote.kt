package ym_cosmetic.pick_perfume_be.vote.entity


import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.vote.entity.vo.VoteCategory
import java.time.LocalDateTime

@Entity
@Table(
    name = "vote",
    uniqueConstraints = [UniqueConstraint(columnNames = ["member_id", "perfume_id", "category"])]
)
class Vote(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    var member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "perfume_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    var perfume: Perfume,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: VoteCategory,

    @Column(nullable = false)
    var value: String,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) : BaseTimeEntity() {
    init {
        require(category.isValidValue(value)) {
            "Invalid vote value '${value}' for category ${category.name}. Allowed values: ${category.getAllowedValues()}"
        }
    }

    fun updateValue(newValue: String) {
        require(category.isValidValue(newValue)) {
            "Invalid vote value '${newValue}' for category ${category.name}. Allowed values: ${category.getAllowedValues()}"
        }
        this.value = newValue
    }
}


