package ym_cosmetic.pick_perfume_be.community.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import ym_cosmetic.pick_perfume_be.member.entity.Member
import java.time.LocalDateTime

@Entity
@Table(name = "post")
@EntityListeners(AuditingEntityListener::class)
class Post private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    val id: Long = 0,

    @Column(nullable = false)
    private var title: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    private var content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "board_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private var board: Board,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @CreatedBy
    @Column(updatable = false)
    val createdBy: String? = null,

    @LastModifiedBy
    @Column
    var updatedBy: String? = null,

    @Column(nullable = false)
    private var isDeleted: Boolean = false,
) {
    companion object {
        fun create(title: String, content: String, member: Member, board: Board): Post {
            require(title.isNotBlank()) { "제목은 비어있을 수 없습니다." }
            require(content.isNotBlank()) { "내용은 비어있을 수 없습니다." }

            return Post(
                title = title,
                content = content,
                member = member,
                board = board
            )
        }
    }

    fun update(title: String, content: String, board: Board): Post {
        require(title.isNotBlank()) { "제목은 비어있을 수 없습니다." }
        require(content.isNotBlank()) { "내용은 비어있을 수 없습니다." }

        this.title = title
        this.content = content
        this.board = board
        this.updatedAt = LocalDateTime.now()

        return this
    }

    fun delete() {
        this.isDeleted = true
    }

    fun restore() {
        this.isDeleted = false
    }

    // 게터
    fun getTitle(): String = this.title
    fun getContent(): String = this.content
    fun getMember(): Member = this.member
    fun getBoard(): Board = this.board
    fun isDeleted(): Boolean = this.isDeleted
} 