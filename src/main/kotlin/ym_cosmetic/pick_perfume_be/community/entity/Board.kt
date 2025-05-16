package ym_cosmetic.pick_perfume_be.community.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "board")
@EntityListeners(AuditingEntityListener::class)
class Board private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    private var name: String,

    @Column(nullable = false)
    private var displayName: String,

    @Column
    private var description: String? = null,

    @Column(nullable = false)
    private var isActive: Boolean = true,

    @Column(nullable = false)
    private var displayOrder: Int = 0,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(
            name: String,
            displayName: String,
            description: String? = null,
            displayOrder: Int = 0
        ): Board {
            require(name.isNotBlank()) { "게시판 이름은 비어있을 수 없습니다." }
            require(displayName.isNotBlank()) { "게시판 표시 이름은 비어있을 수 없습니다." }

            return Board(
                name = name.lowercase().trim(),
                displayName = displayName,
                description = description,
                displayOrder = displayOrder
            )
        }
    }

    fun update(displayName: String, description: String?, displayOrder: Int): Board {
        require(displayName.isNotBlank()) { "게시판 표시 이름은 비어있을 수 없습니다." }

        this.displayName = displayName
        this.description = description
        this.displayOrder = displayOrder
        this.updatedAt = LocalDateTime.now()

        return this
    }

    fun activate() {
        this.isActive = true
    }

    fun deactivate() {
        this.isActive = false
    }

    // 게터
    fun getName(): String = this.name
    fun getDisplayName(): String = this.displayName
    fun getDescription(): String? = this.description
    fun isActive(): Boolean = this.isActive
    fun getDisplayOrder(): Int = this.displayOrder
} 