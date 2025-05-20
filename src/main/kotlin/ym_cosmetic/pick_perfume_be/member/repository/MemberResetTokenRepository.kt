package ym_cosmetic.pick_perfume_be.member.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ym_cosmetic.pick_perfume_be.member.entity.MemberResetToken

interface MemberResetTokenRepository : JpaRepository<MemberResetToken, String> {
    @Query("SELECT t FROM MemberResetToken t WHERE t.email = :email AND t.isUsed = false")
    fun findByEmailAndIsUsedFalse(@Param("email") email: String): List<MemberResetToken>

}