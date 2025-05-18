package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import ym_cosmetic.pick_perfume_be.member.entity.Member
import java.time.LocalDateTime

@Entity
@Table(
    name = "perfume_like",
    uniqueConstraints = [UniqueConstraint(columnNames = ["perfume_id", "member_id"])]
)
@EntityListeners(AuditingEntityListener::class)
class PerfumeLike private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "perfume_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private val perfume: Perfume,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    @CreatedBy
    private val member: Member,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(perfume: Perfume, member: Member): PerfumeLike {
            return PerfumeLike(
                perfume = perfume,
                member = member
            )
        }
    }
}