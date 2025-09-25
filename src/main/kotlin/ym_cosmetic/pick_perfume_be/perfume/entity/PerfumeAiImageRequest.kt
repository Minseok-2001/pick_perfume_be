package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImageProcessStatus
import java.time.LocalDateTime

@Entity
@Table(name = "perfume_ai_image_request")
class PerfumeAiImageRequest private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfume_id", nullable = false)
    val perfume: Perfume,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member? = null,

    @Column(name = "ip_address", length = 64)
    val ipAddress: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PerfumeAiImageProcessStatus,

    @Column(length = 1000)
    var message: String? = null,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null
) : BaseTimeEntity() {

    fun markProcessing() {
        status = PerfumeAiImageProcessStatus.PROCESSING
        message = null
        completedAt = null
    }

    fun markSuccess(message: String? = null) {
        status = PerfumeAiImageProcessStatus.SUCCESS
        this.message = message
        completedAt = LocalDateTime.now()
    }

    fun markSkipped(message: String? = null) {
        status = PerfumeAiImageProcessStatus.SKIPPED
        this.message = message
        completedAt = LocalDateTime.now()
    }

    fun markFailed(message: String?) {
        status = PerfumeAiImageProcessStatus.FAILED
        this.message = message
        completedAt = LocalDateTime.now()
    }

    companion object {
        fun create(
            perfume: Perfume,
            member: Member?,
            ipAddress: String?,
            status: PerfumeAiImageProcessStatus,
            message: String? = null
        ): PerfumeAiImageRequest {
            return PerfumeAiImageRequest(
                perfume = perfume,
                member = member,
                ipAddress = ipAddress,
                status = status,
                message = message
            )
        }
    }
}

