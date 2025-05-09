package ym_cosmetic.pick_perfume_be.designer.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import ym_cosmetic.pick_perfume_be.designer.entity.Designer
import ym_cosmetic.pick_perfume_be.designer.entity.QDesigner
import ym_cosmetic.pick_perfume_be.perfume.entity.QPerfumeDesigner

@Repository
class DesignerRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : DesignerRepositoryCustom {

    override fun findMostProlificDesigners(limit: Int): List<Designer> {
        val designer = QDesigner.designer
        val perfumeDesigner = QPerfumeDesigner.perfumeDesigner

        return queryFactory
            .selectFrom(designer)
            .leftJoin(perfumeDesigner)
            .on(perfumeDesigner.designer.id.eq(designer.id))
            .groupBy(designer.id)
            .orderBy(perfumeDesigner.perfume.id.count().desc())
            .limit(limit.toLong())
            .fetch()
    }
}