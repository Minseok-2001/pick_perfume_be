package ym_cosmetic.pick_perfume_be.community.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import java.time.LocalDateTime

@Entity
@Table(
    name = "post_perfume_embed",
    uniqueConstraints = [UniqueConstraint(columnNames = ["post_id", "perfume_id"])]
)
@EntityListeners(AuditingEntityListener::class)
class PostPerfumeEmbed private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "embed_id")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "post_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private val post: Post,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "perfume_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private val perfume: Perfume,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(post: Post, perfume: Perfume): PostPerfumeEmbed {
            return PostPerfumeEmbed(
                post = post,
                perfume = perfume
            )
        }
    }

    // 게터
    fun getPost(): Post = this.post
    fun getPerfume(): Perfume = this.perfume
} 