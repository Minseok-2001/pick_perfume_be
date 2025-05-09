package ym_cosmetic.pick_perfume_be.member.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.member.enums.ActivityType
import java.time.LocalDateTime

@Entity
@Table(name = "member_activity")
class MemberActivity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val memberId: Long,

    @Column(nullable = false)
    val activityType: ActivityType,

    @Column(nullable = false)
    val perfumeId: Long,

    @Column
    val rating: Int? = null,

    @Column
    val voteCategory: String? = null,

    @Column
    val voteValue: String? = null,

    @Column
    val recommendationType: String? = null, // 추천 타입 (similar, personalized 등)

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now()
)