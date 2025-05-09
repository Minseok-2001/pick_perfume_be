package ym_cosmetic.pick_perfume_be.vote.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.vote.service.VoteStatisticsConverter
import java.time.LocalDateTime

@Entity
@Table(name = "perfume_vote_statistics")
class PerfumeVoteStatistics(
    @Id
    val perfumeId: Long,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "perfume_id", foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    val perfume: Perfume,

    @Column(columnDefinition = "json")
    @Convert(converter = VoteStatisticsConverter::class)
    var statistics: Map<String, Map<String, Int>> = emptyMap(),

    @Column(nullable = false)
    var lastUpdated: LocalDateTime = LocalDateTime.now()
) : BaseTimeEntity()

