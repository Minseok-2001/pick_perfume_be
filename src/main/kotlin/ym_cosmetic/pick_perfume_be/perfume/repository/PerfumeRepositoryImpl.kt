package ym_cosmetic.pick_perfume_be.perfume.repository

import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.brand.entity.QBrand
import ym_cosmetic.pick_perfume_be.member.entity.QMember
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.entity.QPerfume
import ym_cosmetic.pick_perfume_be.review.entity.QReview

@Repository
class PerfumeRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : QuerydslRepositorySupport(Perfume::class.java), PerfumeRepositoryCustom {

    private val perfume = QPerfume.perfume
    private val creator = QMember.member
    private val brand = QBrand.brand
    private val review = QReview.review

    override fun findByIdWithCreatorAndBrand(id: Long): Perfume? {
        return queryFactory
            .selectFrom(perfume)
            .leftJoin(perfume.creator, creator).fetchJoin()
            .leftJoin(perfume.brand, brand).fetchJoin()
            .where(perfume.id.eq(id))
            .fetchOne()
    }

    override fun findAllApprovedWithCreatorAndBrand(pageable: Pageable): Page<Perfume> {
        val query = queryFactory
            .selectFrom(perfume)
            .leftJoin(perfume.creator, creator).fetchJoin()
            .leftJoin(perfume.brand, brand).fetchJoin()
            .where(perfume.isApproved.isTrue)
            .orderBy(perfume.id.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        val content = query.fetch()
        val total = countQuery(perfume.isApproved.isTrue)

        return PageImpl(content, pageable, total)
    }

    override fun findByNameContainingOrBrandNameContaining(
        name: String,
        brandName: String,
        pageable: Pageable
    ): Page<Perfume> {
        val condition = perfume.name.containsIgnoreCase(name)
            .or(perfume.brand.name.containsIgnoreCase(brandName))

        val query = queryFactory
            .selectFrom(perfume)
            .leftJoin(perfume.brand, brand).fetchJoin()
            .where(condition)
            .orderBy(perfume.id.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        val content = query.fetch()
        val total = countQuery(condition)

        return PageImpl(content, pageable, total)
    }

    override fun findByIdWithCreator(id: Long): Perfume? {
        return queryFactory
            .selectFrom(perfume)
            .leftJoin(perfume.creator, creator).fetchJoin()
            .where(perfume.id.eq(id))
            .fetchOne()
    }

    override fun findAllApprovedWithCreator(pageable: Pageable): Page<Perfume> {
        val query = queryFactory
            .selectFrom(perfume)
            .leftJoin(perfume.creator, creator).fetchJoin()
            .where(perfume.isApproved.isTrue)
            .orderBy(perfume.id.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        val content = query.fetch()
        val total = countQuery(perfume.isApproved.isTrue)

        return PageImpl(content, pageable, total)
    }

    override fun findRecentlyAdded(pageable: Pageable): Page<Perfume> {
        val query = queryFactory
            .selectFrom(perfume)
            .where(perfume.isApproved.isTrue)
            .orderBy(perfume.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        val content = query.fetch()
        val total = countQuery(perfume.isApproved.isTrue)

        return PageImpl(content, pageable, total)
    }

    override fun findByBrandNameOrderByAverageRatingDesc(
        brandName: String,
        pageable: Pageable
    ): Page<Perfume> {
        // 평균 평점 서브쿼리
        val avgRatingSubquery = JPAExpressions
            .select(review.rating.value.avg())
            .from(review)
            .where(review.perfume.id.eq(perfume.id))
            .groupBy(review.perfume.id)

        val avgRatingOrderSpecifier = OrderSpecifier(
            Order.DESC,
            Expressions.numberTemplate(Double::class.java, "({0})", avgRatingSubquery)
        )

        val query = queryFactory
            .selectFrom(perfume)
            .leftJoin(perfume.brand, brand).fetchJoin()
            .where(perfume.brand.name.eq(brandName))
            .orderBy(
                avgRatingOrderSpecifier.nullsLast(),
                perfume.id.asc()
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        val content = query.fetch()
        val total = countQuery(perfume.brand.name.eq(brandName))

        return PageImpl(content, pageable, total)
    }

    override fun findTopByReviewCount(pageable: Pageable): Page<Perfume> {
        val reviewCountSubquery = JPAExpressions
            .select(review.count())
            .from(review)
            .where(review.perfume.id.eq(perfume.id))
            .groupBy(review.perfume.id)

        val reviewCountOrderSpecifier = OrderSpecifier(
            Order.DESC,
            Expressions.numberTemplate(Long::class.java, "({0})", reviewCountSubquery)
        )

        val query = queryFactory
            .selectFrom(perfume)
            .orderBy(
                reviewCountOrderSpecifier.nullsLast(),
                perfume.id.asc()
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        val content = query.fetch()
        val total = countQuery(null)

        return PageImpl(content, pageable, total)
    }

    private fun countQuery(condition: BooleanExpression?): Long {
        val query = queryFactory
            .select(perfume.count())
            .from(perfume)

        if (condition != null) {
            query.where(condition)
        }

        return query.fetchOne() ?: 0L
    }
}