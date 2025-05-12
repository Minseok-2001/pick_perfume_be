package ym_cosmetic.pick_perfume_be.perfume.repository

import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.hibernate.Hibernate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.brand.entity.QBrand
import ym_cosmetic.pick_perfume_be.member.entity.QMember
import ym_cosmetic.pick_perfume_be.perfume.entity.*
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

    override fun findAllWithDetails(): List<Perfume> {
        // 1. 기본 향수 정보 로드
        val perfumes = JPAQueryFactory(entityManager)
            .selectFrom(QPerfume.perfume)
            .fetch()
        
        // 2. 향수 ID 목록 추출
        val perfumeIds = perfumes.map { it.id }
        
        // 3. 각 관계를 별도로 로드하고 메모리에서 결합
        if (perfumeIds.isNotEmpty()) {
            // 3.1 노트 로드
            JPAQueryFactory(entityManager)
                .selectFrom(QPerfumeNote.perfumeNote)
                .where(QPerfumeNote.perfumeNote.perfume.id.`in`(perfumeIds))
                .fetch()
                .groupBy { it.perfume.id }
            
            // 3.2 어코드 로드
            JPAQueryFactory(entityManager)
                .selectFrom(QPerfumeAccord.perfumeAccord)
                .where(QPerfumeAccord.perfumeAccord.perfume.id.`in`(perfumeIds))
                .fetch()
                .groupBy { it.perfume.id }
            
            // 3.3 디자이너 로드
            JPAQueryFactory(entityManager)
                .selectFrom(QPerfumeDesigner.perfumeDesigner)
                .where(QPerfumeDesigner.perfumeDesigner.perfume.id.`in`(perfumeIds))
                .fetch()
                .groupBy { it.perfume.id }
            
            // 4. Hibernate 초기화 (LazyInitializationException 방지)
            perfumes.forEach { perfume ->
                // 각 관계를 초기화
                Hibernate.initialize(perfume.perfumeNotes)
                Hibernate.initialize(perfume.perfumeAccords)
                Hibernate.initialize(perfume.designers)
            }
        }
        
        return perfumes
    }
    
    /**
     * 브랜드 정보를 포함한 향수 ID 목록으로 조회
     */
    @Transactional(readOnly = true)
    override fun findAllByIdsWithBrand(ids: List<Long>): List<Perfume> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        
        // 향수와 브랜드 정보 로드
        val result = queryFactory
            .selectFrom(perfume)
            .leftJoin(perfume.brand, brand).fetchJoin()
            .where(perfume.id.`in`(ids))
            .fetch()
            
        // 리뷰 정보를 따로 로드하여 메모리에 준비
        val reviewsByPerfumeId = queryFactory
            .selectFrom(review)
            .where(review.perfume.id.`in`(ids))
            .fetch()
            .groupBy { it.perfume.id }
            
        // 연관 엔티티 초기화
        result.forEach { perfume ->
            // 브랜드 속성 초기화
            Hibernate.initialize(perfume.brand)
            if (perfume.brand.country != null) {
                Hibernate.initialize(perfume.brand.country)
            }
            if (perfume.brand.logo != null) {
                Hibernate.initialize(perfume.brand.logo)
            }
            
            // 이미지 초기화
            if (perfume.image != null) {
                Hibernate.initialize(perfume.image)
            }
            
            // 디자이너 컬렉션 초기화를 시도 (하지만 초기화 실패해도 에러 발생 안함)
            try {
                Hibernate.initialize(perfume.designers)
            } catch (e: Exception) {
                // 초기화 실패해도 무시
            }
            
            // reviews 컬렉션의 사이즈를 먼저 확인하여 안전하게 접근
            val reviewsSize = queryFactory
                .select(review.count())
                .from(review)
                .where(review.perfume.id.eq(perfume.id!!))
                .fetchOne() ?: 0L
            
            // 컬렉션에 대한 연산을 통해 초기화 (직접 Hibernate.initialize 호출 대신)
            perfume.reviews.size // 크기만 확인해도 초기화됨
        }
        
        return result
    }

}