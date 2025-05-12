package ym_cosmetic.pick_perfume_be.batch.processor

import org.slf4j.LoggerFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.search.document.*
import java.time.LocalDateTime

@Component
class PerfumeIndexProcessor : ItemProcessor<Perfume, PerfumeDocument> {

    private val logger = LoggerFactory.getLogger(PerfumeIndexProcessor::class.java)

    override fun process(perfume: Perfume): PerfumeDocument? {
        try {
            val brandName = perfume.brand.name
            val brandId = perfume.brand.id ?: 0L

            // 노트 정보 구성
            val notes = perfume.getNotes().map { perfumeNote ->
                NoteDocument(
                    id = perfumeNote.note.id ?: 0L,
                    name = perfumeNote.note.name,
                    type = perfumeNote.type.name
                )
            }

            // 어코드 정보 구성
            val accords = perfume.getAccords().map { perfumeAccord ->
                AccordDocument(
                    id = perfumeAccord.accord.id ?: 0L,
                    name = perfumeAccord.accord.name
                )
            }

            // 디자이너 정보 구성
            val designers = perfume.designers.map { perfumeDesigner ->
                DesignerDocument(
                    id = perfumeDesigner.designer.id ?: 0L,
                    name = perfumeDesigner.designer.name,
                    role = perfumeDesigner.designerRole.name
                )
            }

            // 계절감 정보 구성 - 투표 데이터가 있을 경우에만
            val seasonVotes = perfume.getVoteResults()
                .entries
                .find { it.key.name == "SEASON" }
                ?.value

            val seasonality = seasonVotes?.let {
                Seasonality(
                    spring = calculateSeasonScore(it, "SPRING"),
                    summer = calculateSeasonScore(it, "SUMMER"),
                    fall = calculateSeasonScore(it, "FALL"),
                    winter = calculateSeasonScore(it, "WINTER")
                )
            }

            // 성별 정보 추출 - 투표 데이터가 있을 경우에만
            val genderVotes = perfume.getVoteResults()
                .entries
                .find { it.key.name == "GENDER" }
                ?.value

            val gender = genderVotes?.let {
                it.entries
                    .maxByOrNull { entry -> entry.value }
                    ?.key
            }

            // 평균 평점 계산
            val averageRating = perfume.calculateAverageRating()
            val reviewCount = perfume.getReviewCount()

            // 최종 문서 생성
            return PerfumeDocument(
                id = perfume.id ?: 0L,
                name = perfume.name,
                content = perfume.content ?: "",
                releaseYear = perfume.releaseYear,
                brandName = brandName,
                brandId = brandId,
                concentration = perfume.concentration?.name,
                imageUrl = perfume.image?.url,
                notes = notes,
                accords = accords,
                designers = designers,
                averageRating = averageRating,
                reviewCount = reviewCount,
                isApproved = perfume.isApproved,
                seasonality = seasonality,
                gender = gender,
                createdAt = perfume.createdAt ?: LocalDateTime.now(),
                updatedAt = perfume.updatedAt ?: LocalDateTime.now()
            )

        } catch (e: Exception) {
            logger.error("향수 문서 처리 중 오류 발생: ${perfume.name}", e)
            return null
        }
    }

    private fun calculateSeasonScore(seasonVotes: Map<String, Int>, season: String): Float {
        val totalVotes = seasonVotes.values.sum().toFloat()
        if (totalVotes == 0f) return 0f

        return (seasonVotes[season] ?: 0).toFloat() / totalVotes
    }
} 