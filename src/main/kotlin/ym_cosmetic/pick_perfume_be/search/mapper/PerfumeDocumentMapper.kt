package ym_cosmetic.pick_perfume_be.search.mapper

import org.springframework.stereotype.Component
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.search.document.AccordDocument
import ym_cosmetic.pick_perfume_be.search.document.DesignerDocument
import ym_cosmetic.pick_perfume_be.search.document.NoteDocument
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument

@Component
class PerfumeDocumentMapper {
    fun toDocument(perfume: Perfume): PerfumeDocument {
        // 노트 가져오기
        val notes = perfume.getNotes().map { perfumeNote ->
            NoteDocument(
                id = perfumeNote.note.id ?: 0L,
                name = perfumeNote.note.name,
                type = perfumeNote.type.name
            )
        }

        // 어코드 가져오기
        val accords = perfume.getAccords().map { perfumeAccord ->
            AccordDocument(
                id = perfumeAccord.accord.id ?: 0L,
                name = perfumeAccord.accord.name
            )
        }

        // 디자이너 정보 가져오기
        val designers = perfume.designers.map { perfumeDesigner ->
            DesignerDocument(
                id = perfumeDesigner.designer.id ?: 0L,
                name = perfumeDesigner.designer.name,
                role = perfumeDesigner.role.name
            )
        }

        return PerfumeDocument(
            id = perfume.id ?: 0L,
            name = perfume.name,
            description = perfume.description,
            brandName = perfume.brand.name,
            brandId = perfume.brand.id ?: 0L,
            releaseYear = perfume.releaseYear,
            concentration = perfume.concentration?.name,
            imageUrl = perfume.image?.url,
            notes = notes,
            accords = accords,
            designers = designers,
            averageRating = perfume.calculateAverageRating(),
            reviewCount = perfume.getReviewCount(),
            isApproved = perfume.isApproved,
            createdAt = perfume.createdAt,
            updatedAt = perfume.updatedAt
        )
    }
}