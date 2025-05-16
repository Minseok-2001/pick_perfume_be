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
@Table(name = "comment")
@EntityListeners(AuditingEntityListener::class)
class Comment private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    val id: Long = 0,

    @Column(columnDefinition = "TEXT", nullable = false)
    private var content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "post_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private val post: Post,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private val parent: Comment? = null,

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL])
    private val replies: MutableList<Comment> = mutableListOf(),

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
    private var isDeleted: Boolean = false
) {
    companion object {
        fun create(content: String, post: Post, member: Member, parent: Comment? = null): Comment {
            require(content.isNotBlank()) { "내용은 비어있을 수 없습니다." }

            return Comment(
                content = content,
                post = post,
                member = member,
                parent = parent
            )
        }
    }

    fun update(content: String): Comment {
        require(content.isNotBlank()) { "내용은 비어있을 수 없습니다." }

        this.content = content
        this.updatedAt = LocalDateTime.now()

        return this
    }

    fun delete() {
        this.isDeleted = true
    }

    fun restore() {
        this.isDeleted = false
    }

    fun addReply(reply: Comment) {
        replies.add(reply)
    }

    // 게터
    fun getContent(): String = this.content
    fun getPost(): Post = this.post
    fun getMember(): Member = this.member
    fun getParent(): Comment? = this.parent
    fun getReplies(): List<Comment> = this.replies.toList()
    fun isDeleted(): Boolean = this.isDeleted
} 