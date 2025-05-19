package ym_cosmetic.pick_perfume_be.community.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ym_cosmetic.pick_perfume_be.community.entity.PostView
import java.time.LocalDate

interface PostViewRepository : JpaRepository<PostView, Long> {
    // 특정 게시물의 총 조회수 집계
    @Query("SELECT COUNT(pv) FROM PostView pv WHERE pv.post.id = :postId")
    fun countByPostId(postId: Long): Long
    
    // 특정 날짜에 특정 사용자가 게시물을 이미 조회했는지 확인
    fun existsByPostIdAndMemberIdAndViewDate(
        postId: Long, 
        memberId: Long, 
        viewDate: LocalDate
    ): Boolean
    
    // 특정 날짜에 특정 IP가 게시물을 이미 조회했는지 확인
    fun existsByPostIdAndIpAddressAndViewDate(
        postId: Long, 
        ipAddress: String, 
        viewDate: LocalDate
    ): Boolean
    
    // 특정 날짜에 특정 게시물의 조회수 집계
    fun countByPostIdAndViewDate(postId: Long, viewDate: LocalDate): Long
    
    // 특정 기간 내 게시물 조회수 집계
    @Query("SELECT COUNT(pv) FROM PostView pv WHERE pv.post.id = :postId AND pv.viewDate BETWEEN :startDate AND :endDate")
    fun countByPostIdAndViewDateBetween(
        postId: Long, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): Long
    
    // 여러 게시물 ID들에 대한 조회수 집계
    @Query("SELECT pv.post.id, COUNT(pv) FROM PostView pv WHERE pv.post.id IN :postIds GROUP BY pv.post.id")
    fun countByPostIdIn(postIds: List<Long>): Map<Long, Long>
} 