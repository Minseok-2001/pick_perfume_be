package ym_cosmetic.pick_perfume_be.review.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import ym_cosmetic.pick_perfume_be.member.entity.Member
import java.time.LocalDateTime

@Entity
@Table(
    name = "review_reactions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["member_id", "review_id"])]
)
class ReviewReaction(
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
        name = "review_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    var review: Review,

    @Column(nullable = false)
    var isLike: Boolean,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)