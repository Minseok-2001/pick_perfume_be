package ym_cosmetic.pick_perfume_be.community.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.community.entity.Post
import java.util.*

@Repository
interface PostRepository : JpaRepository<Post, Long>, PostRepositoryCustom {

    @EntityGraph(attributePaths = ["member"])
    fun findByIdAndIsDeletedFalse(id: Long): Optional<Post>

    @EntityGraph(attributePaths = ["member"])
    fun findByIsDeletedFalse(pageable: Pageable): Page<Post>

    @EntityGraph(attributePaths = ["member", "board"])
    fun findByBoardIdAndIsDeletedFalse(boardId: Long, pageable: Pageable): Page<Post>

    @Query("SELECT p FROM Post p JOIN p.member m WHERE p.isDeleted = false AND m.id = :memberId")
    @EntityGraph(attributePaths = ["member", "board"])
    fun findByMemberIdAndIsDeletedFalse(
        @Param("memberId") memberId: Long,
        pageable: Pageable
    ): Page<Post>


} 