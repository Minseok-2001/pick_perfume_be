package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.vo.Concentration

@Entity
@Table(name = "perfumes")
class Perfume(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var brand: String,

    @Column(length = 5000)
    var description: String? = null,

    @Column
    var releaseYear: Int? = null,

    @Column
    var perfumer: String? = null,

    @Enumerated(EnumType.STRING)
    @Column
    var concentration: Concentration? = null,

    @Embedded
    var image: ImageUrl? = null,

    @Column(nullable = false)
    var isApproved: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "creator_id", foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    var creator: Member? = null,
) : BaseTimeEntity() {
    fun approve() {
        if (!isApproved) {
            isApproved = true
        }
    }

    fun updateDetails(
        name: String,
        brand: String,
        description: String?,
        releaseYear: Int?,
        perfumer: String?,
        concentration: Concentration?
    ) {
        this.name = name
        this.brand = brand
        this.description = description
        this.releaseYear = releaseYear
        this.perfumer = perfumer
        this.concentration = concentration
    }

    fun updateImage(image: ImageUrl?) {
        this.image = image
    }

    fun getNotes(): List<PerfumeNote> {
        // 이 메서드는 실제로는 지연 로딩된 컬렉션을 로드하거나
        // JPA Repository에서 관련 데이터를 가져오는 로직이 구현되어야 함
        // 여기서는 간단한 구현만 보여줌
        return emptyList() // 실제 구현에서는 연관된 PerfumeNote를 반환
    }

    fun getAccords(): List<PerfumeAccord> {
        // 이 메서드는 실제로는 지연 로딩된 컬렉션을 로드하거나
        // JPA Repository에서 관련 데이터를 가져오는 로직이 구현되어야 함
        return emptyList() // 실제 구현에서는 연관된 PerfumeAccord를 반환
    }

    fun calculateAverageRating(): Double {
        // 실제 구현에서는 리뷰 리포지토리를 통해 계산
        return 0.0
    }

    fun getReviewCount(): Int {
        // 실제 구현에서는 리뷰 리포지토리를 통해 계산
        return 0
    }

}