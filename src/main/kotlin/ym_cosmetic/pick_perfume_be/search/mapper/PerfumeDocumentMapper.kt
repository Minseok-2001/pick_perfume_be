package ym_cosmetic.pick_perfume_be.search.mapper

import org.springframework.stereotype.Component
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType
import ym_cosmetic.pick_perfume_be.search.document.DesignerInfo
import ym_cosmetic.pick_perfume_be.search.document.NotesByType
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument

@Component
class PerfumeDocumentMapper {
    fun toDocument(perfume: Perfume): PerfumeDocument {
        // 노트 가져오기
        val perfumeNotes = perfume.getNotes()
        val allNotes = perfumeNotes.map { it.note.name }

        val notesByType = listOf(
            NotesByType(
                type = "TOP",
                notes = perfumeNotes.filter { it.type == NoteType.TOP }.map { it.note.name }
            ),
            NotesByType(
                type = "MIDDLE",
                notes = perfumeNotes.filter { it.type == NoteType.MIDDLE }.map { it.note.name }
            ),
            NotesByType(
                type = "BASE",
                notes = perfumeNotes.filter { it.type == NoteType.BASE }.map { it.note.name }
            )
        )

        // 어코드 가져오기
        val accords = perfume.getAccords().map { it.accord.name }

        // 디자이너 정보 가져오기
        val designers = perfume.designers.map {
            DesignerInfo(
                id = it.designer.id.toString(),
                name = it.designer.name,
                role = it.role.name
            )
        }

        // 투표 결과 집계
        val voteResults = perfume.getVoteResults()

        return PerfumeDocument(
            id = perfume.id.toString(),
            name = perfume.name,
            description = perfume.description,
            brandName = perfume.brand.name,
            releaseYear = perfume.releaseYear,
            concentration = perfume.concentration?.name,
            notes = allNotes,
            notesByType = notesByType,
            accords = accords,
            designers = designers,
            averageRating = perfume.calculateAverageRating().toFloat(),
            reviewCount = perfume.getReviewCount(),
            voteResults = voteResults,
            releaseDate = perfume.releaseYear?.let { java.time.LocalDate.of(it, 1, 1) },
            createdAt = perfume.createdAt,
            updatedAt = perfume.updatedAt
        )
    }
}