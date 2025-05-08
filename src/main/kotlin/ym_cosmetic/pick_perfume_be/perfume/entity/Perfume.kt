package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.perfume.vo.Concentration
import java.time.LocalDateTime

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

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
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
}