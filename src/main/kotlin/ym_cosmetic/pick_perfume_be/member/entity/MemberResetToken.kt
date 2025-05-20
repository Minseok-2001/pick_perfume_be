package ym_cosmetic.pick_perfume_be.member.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity


@Entity
@Table(name = "member_reset_token")
class MemberResetToken (
    @Id
    @Column(nullable = false)
    var token: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    val member: Member,

    @Column(nullable = false)
    var email: String,

    @Column(nullable = false)
    var expirationDate: Long,

    @Column(nullable = false)
    var isUsed: Boolean = false,


    ) : BaseTimeEntity()