package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.member.entity.Member

@Entity
@Table(
    name = "perfume_ai_image_vote",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_perfume_ai_image_vote_member",
            columnNames = ["perfume_id", "member_id"]
        )
    ]
)
class PerfumeAiImageVote private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfume_id", nullable = false)
    val perfume: Perfume,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfume_ai_image_id", nullable = false)
    var aiImage: PerfumeAiImage,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null,

    @Column(name = "ip_address", length = 64)
    val ipAddress: String? = null
) : BaseTimeEntity() {

    companion object {
        fun create(
            perfume: Perfume,
            aiImage: PerfumeAiImage,
            member: Member?,
            ipAddress: String?
        ): PerfumeAiImageVote {
            return PerfumeAiImageVote(
                perfume = perfume,
                aiImage = aiImage,
                member = member,
                ipAddress = ipAddress
            )
        }

    }

    fun changeSelection(newImage: PerfumeAiImage) {
        if (aiImage.id != newImage.id) {
            aiImage = newImage
        }
    }

    fun attachMemberIfMissing(newMember: Member?) {
        if (member == null && newMember != null) {
            member = newMember
        }
    }
}
