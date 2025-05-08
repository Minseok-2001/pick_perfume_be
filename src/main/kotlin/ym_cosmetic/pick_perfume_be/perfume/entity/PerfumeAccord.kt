package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import ym_cosmetic.pick_perfume_be.accord.entity.Accord
import java.time.LocalDateTime

@Entity
@Table(name = "perfume_accord")
class PerfumeAccord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "perfume_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    var perfume: Perfume,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "accord_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    var accord: Accord,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)