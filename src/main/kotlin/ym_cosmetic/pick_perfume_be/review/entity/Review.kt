package ym_cosmetic.pick_perfume_be.review.entity


import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.vo.Season
import ym_cosmetic.pick_perfume_be.review.vo.Rating
import ym_cosmetic.pick_perfume_be.review.vo.Sentiment
import ym_cosmetic.pick_perfume_be.review.vo.TimeOfDay
import java.time.LocalDateTime

@Entity
@Table(name = "reviews")
class Review(
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
        name = "perfume_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    var perfume: Perfume,

    @Column(nullable = false, length = 10000)
    var content: String,

    @Embedded
    var rating: Rating,

    @Enumerated(EnumType.STRING)
    @Column
    var season: Season? = null,

    @Enumerated(EnumType.STRING)
    @Column
    var timeOfDay: TimeOfDay? = null,

    @Enumerated(EnumType.STRING)
    @Column
    var sentiment: Sentiment? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(
        content: String, rating: Rating, season: Season?,
        timeOfDay: TimeOfDay?, sentiment: Sentiment?
    ) {
        this.content = content
        this.rating = rating
        this.season = season
        this.timeOfDay = timeOfDay
        this.sentiment = sentiment
    }
}