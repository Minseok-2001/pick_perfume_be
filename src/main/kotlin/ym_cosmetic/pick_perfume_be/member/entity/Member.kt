package ym_cosmetic.pick_perfume_be.member.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "members")
class Member(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var nickname: String,

    @Embedded
    var profileImage: ImageUrl? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: AuthProvider = AuthProvider.LOCAL,

    @Column
    var providerId: String? = null,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    enum class Role {
        USER, ADMIN
    }

    enum class AuthProvider {
        LOCAL, KAKAO, NAVER
    }

    // 도메인 로직
    fun updateProfile(nickname: String, profileImage: ImageUrl?) {
        this.nickname = nickname
        this.profileImage = profileImage
    }

    fun isAdmin(): Boolean = role == Role.USER
}