package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import ym_cosmetic.pick_perfume_be.member.entity.Member
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "perfume_view",
    uniqueConstraints = [UniqueConstraint(columnNames = ["perfume_id", "member_id", "view_date"])]
)
@EntityListeners(AuditingEntityListener::class)
class PerfumeView private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_id")
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
        nullable = true,  // 비회원도 조회 가능하도록 null 허용
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private val member: Member?,
    
    @Column(name = "view_date", nullable = false)
    val viewDate: LocalDate = LocalDate.now(),
    
    @Column(name = "ip_address", nullable = true, length = 45)
    val ipAddress: String? = null,
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(
            perfume: Perfume, 
            member: Member?, 
            ipAddress: String? = null
        ): PerfumeView {
            return PerfumeView(
                perfume = perfume,
                member = member,
                ipAddress = ipAddress
            )
        }
    }
} 