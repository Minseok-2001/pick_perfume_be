package ym_cosmetic.pick_perfume_be.member.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.member.enums.AuthProvider
import ym_cosmetic.pick_perfume_be.member.enums.MemberRole
import ym_cosmetic.pick_perfume_be.security.PasswordEncoder

@Entity
@Table(name = "member")
class Member(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column
    var password: String? = null,

    @Column(nullable = false)
    var nickname: String,

    @Column
    var name: String? = null,

    @Embedded
    var profileImage: ImageUrl? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: MemberRole = MemberRole.MEMBER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: AuthProvider = AuthProvider.LOCAL,

    @Column
    var providerId: String? = null,

    @Column
    var phoneNumber: String? = null,

    ) : BaseTimeEntity() {

    fun updateProfile(nickname: String, profileImage: ImageUrl?) {
        this.nickname = nickname
        this.profileImage = profileImage
    }

    fun isCredentialValid(password: String, passwordEncoder: PasswordEncoder): Boolean {
        return passwordEncoder.matches(password, this.password)
    }

    fun isAdmin(): Boolean = role == MemberRole.ADMIN
}