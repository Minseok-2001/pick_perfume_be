package ym_cosmetic.pick_perfume_be.community.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import ym_cosmetic.pick_perfume_be.community.enums.ReportStatus
import ym_cosmetic.pick_perfume_be.community.enums.ReportTargetType
import ym_cosmetic.pick_perfume_be.community.enums.ReportType
import ym_cosmetic.pick_perfume_be.member.entity.Member
import java.time.LocalDateTime

@Entity
@Table(name = "report")
@EntityListeners(AuditingEntityListener::class)
class Report private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "reporter_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private val reporter: Member,
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private var reportType: ReportType,
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private val targetType: ReportTargetType,
    
    @Column(nullable = false)
    private val targetId: Long,
    
    @Column(columnDefinition = "TEXT")
    private var content: String?,
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private var status: ReportStatus = ReportStatus.REPORTED,
    
    @Column
    private var processorComment: String? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "processor_id",
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private var processor: Member? = null,
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(
            reporter: Member,
            reportType: ReportType,
            targetType: ReportTargetType,
            targetId: Long,
            content: String?
        ): Report {
            return Report(
                reporter = reporter,
                reportType = reportType,
                targetType = targetType,
                targetId = targetId,
                content = content
            )
        }
    }
    
    // 신고 상태 변경
    fun updateStatus(status: ReportStatus, processor: Member, comment: String?): Report {
        this.status = status
        this.processor = processor
        this.processorComment = comment
        this.updatedAt = LocalDateTime.now()
        return this
    }
    
    // Getters
    fun getReporter(): Member = this.reporter
    fun getReportType(): ReportType = this.reportType
    fun getTargetType(): ReportTargetType = this.targetType
    fun getTargetId(): Long = this.targetId
    fun getContent(): String? = this.content
    fun getStatus(): ReportStatus = this.status
    fun getProcessorComment(): String? = this.processorComment
    fun getProcessor(): Member? = this.processor
} 