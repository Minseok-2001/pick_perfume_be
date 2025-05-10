package ym_cosmetic.pick_perfume_be.batch.processor

import org.slf4j.LoggerFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.accord.entity.Accord
import ym_cosmetic.pick_perfume_be.accord.repository.AccordRepository
import ym_cosmetic.pick_perfume_be.batch.dto.PerfumeImportDto
import ym_cosmetic.pick_perfume_be.brand.entity.Brand
import ym_cosmetic.pick_perfume_be.brand.repository.BrandRepository
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.designer.entity.Designer
import ym_cosmetic.pick_perfume_be.designer.repository.DesignerRepository
import ym_cosmetic.pick_perfume_be.note.entity.Note
import ym_cosmetic.pick_perfume_be.note.repository.NoteRepository
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.enums.DesignerRole
import ym_cosmetic.pick_perfume_be.perfume.vo.Concentration
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType

@Component
class PerfumeProcessor(
    private val brandRepository: BrandRepository,
    private val noteRepository: NoteRepository,
    private val accordRepository: AccordRepository,
    private val designerRepository: DesignerRepository
) : ItemProcessor<PerfumeImportDto, Perfume> {

    private val logger = LoggerFactory.getLogger(PerfumeProcessor::class.java)

    @Transactional
    override fun process(item: PerfumeImportDto): Perfume? {
        try {
            // 브랜드 조회 또는 생성
            val brand = findOrCreateBrand(item.brandName)

            // 농도 파싱
            val concentration = parseConcentration(item.concentration)

            // 이미지 URL
            val imageUrl = item.imageUrl?.let { ImageUrl(it) }

            // 향수 생성
            val perfume = Perfume.create(
                name = item.name,
                brand = brand,
                description = item.description,
                releaseYear = item.releaseYear,
                concentration = concentration,
                image = imageUrl,
                isAdmin = true,
                searchSynced = false
            )

            // 노트 처리
            processNotes(perfume, item)

            // 어코드 처리
            processAccords(perfume, item)

            // 조향사 처리
            processPerfumer(perfume, item)

            return perfume

        } catch (e: Exception) {
            logger.error("향수 처리 중 오류 발생: ${item.name}", e)
            return null
        }
    }

    private fun findOrCreateBrand(brandName: String): Brand {
        return brandRepository.findByNameIgnoreCase(brandName) ?: brandRepository.save(
            Brand.create(name = brandName)
        )
    }

    private fun parseConcentration(concentration: String?): Concentration? {
        return when (concentration?.trim()?.uppercase()) {
            "EDP", "EAU DE PARFUM" -> Concentration.EAU_DE_PARFUM
            "EDT", "EAU DE TOILETTE" -> Concentration.EAU_DE_TOILETTE
            "EDC", "EAU DE COLOGNE" -> Concentration.EAU_DE_COLOGNE
            "PARFUM", "EXTRAIT", "EXTRACT", "PERFUME" -> Concentration.PARFUM
            "BODY SPRAY", "BODY MIST" -> Concentration.BODY_SPRAY
            null -> null
            else -> null
        }
    }

    private fun processNotes(perfume: Perfume, item: PerfumeImportDto) {
        // 탑 노트 처리
        item.topNotes?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?.forEach { noteName ->
                val note = findOrCreateNote(noteName)
                perfume.addNote(note, NoteType.TOP)
            }

        // 미들 노트 처리
        item.middleNotes?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?.forEach { noteName ->
                val note = findOrCreateNote(noteName)
                perfume.addNote(note, NoteType.MIDDLE)
            }

        // 베이스 노트 처리
        item.baseNotes?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?.forEach { noteName ->
                val note = findOrCreateNote(noteName)
                perfume.addNote(note, NoteType.BASE)
            }
    }

    private fun findOrCreateNote(noteName: String): Note {
        return noteRepository.findByNameIgnoreCase(noteName) ?: noteRepository.save(
            Note.create(name = noteName)
        )
    }

    private fun processAccords(perfume: Perfume, item: PerfumeImportDto) {
        item.accords?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?.forEach { accordName ->
                val accord = findOrCreateAccord(accordName)
                perfume.addAccord(accord)
            }
    }

    private fun findOrCreateAccord(accordName: String): Accord {
        return accordRepository.findByNameIgnoreCase(accordName) ?: accordRepository.save(
            Accord.create(name = accordName)
        )
    }

    private fun processPerfumer(perfume: Perfume, item: PerfumeImportDto) {
        item.perfumer?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?.forEach { perfumerName ->
                val designer = findOrCreateDesigner(perfumerName)
                perfume.addDesigner(designer, DesignerRole.PERFUMER)
            }
    }

    private fun findOrCreateDesigner(designerName: String): Designer {
        return designerRepository.findByNameIgnoreCase(designerName) ?: designerRepository.save(
            Designer.create(name = designerName)
        )
    }
} 