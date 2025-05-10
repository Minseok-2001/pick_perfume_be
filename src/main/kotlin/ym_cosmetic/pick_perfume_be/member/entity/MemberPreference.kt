package ym_cosmetic.pick_perfume_be.member.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.member.converter.LongListConverter
import ym_cosmetic.pick_perfume_be.member.converter.StringListConverter
import java.time.LocalDateTime

@Entity
@Table(name = "member_preference")
class MemberPreference(
    @Id
    val memberId: Long,

    @OneToOne
    @MapsId
    @JoinColumn(name = "member_id", foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    val member: Member,

    @Column(columnDefinition = "json")
    @Convert(converter = StringListConverter::class)
    var preferredNotes: List<String> = emptyList(),

    @Column(columnDefinition = "json")
    @Convert(converter = StringListConverter::class)
    var preferredAccords: List<String> = emptyList(),

    @Column(columnDefinition = "json")
    @Convert(converter = StringListConverter::class)
    var preferredBrands: List<String> = emptyList(),

    @Column(columnDefinition = "json")
    @Convert(converter = LongListConverter::class)
    var reviewedPerfumeIds: List<Long> = emptyList(),

    @Column(nullable = false)
    var lastUpdated: LocalDateTime = LocalDateTime.now(),

    @Version
    var version: Long = 0,
) : BaseTimeEntity()

