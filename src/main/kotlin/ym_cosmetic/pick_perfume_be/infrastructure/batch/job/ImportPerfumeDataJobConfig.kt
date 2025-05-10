package ym_cosmetic.pick_perfume_be.infrastructure.batch.job

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import ym_cosmetic.pick_perfume_be.accord.entity.Accord
import ym_cosmetic.pick_perfume_be.accord.repository.AccordRepository
import ym_cosmetic.pick_perfume_be.brand.repository.BrandRepository
import ym_cosmetic.pick_perfume_be.designer.entity.Designer
import ym_cosmetic.pick_perfume_be.designer.repository.DesignerRepository
import ym_cosmetic.pick_perfume_be.infrastructure.batch.model.PerfumeAccordCSV
import ym_cosmetic.pick_perfume_be.infrastructure.batch.model.PerfumeCSV
import ym_cosmetic.pick_perfume_be.infrastructure.batch.model.PerfumeNoteCSV
import ym_cosmetic.pick_perfume_be.infrastructure.batch.reader.CSVItemReaderFactory
import ym_cosmetic.pick_perfume_be.note.entity.Note
import ym_cosmetic.pick_perfume_be.note.repository.NoteRepository
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeAccord
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeDesigner
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeNote
import ym_cosmetic.pick_perfume_be.perfume.enums.DesignerRole
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeAccordRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeDesignerRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeNoteRepository
import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType
import jakarta.persistence.EntityManagerFactory

@Configuration
class ImportPerfumeDataJobConfig(
    private val jobRepository: JobRepository,
    private val entityManagerFactory: EntityManagerFactory,
    private val taskExecutor: TaskExecutor,
    private val transactionManager: PlatformTransactionManager,
    private val perfumeRepository: PerfumeRepository,
    private val noteRepository: NoteRepository,
    private val accordRepository: AccordRepository,
    private val brandRepository: BrandRepository,
    private val designerRepository: DesignerRepository,
    private val perfumeNoteRepository: PerfumeNoteRepository,
    private val perfumeAccordRepository: PerfumeAccordRepository,
    private val perfumeDesignerRepository: PerfumeDesignerRepository
) {

    @Bean
    fun importPerfumeDataJob(): Job {
        return JobBuilder("importPerfumeDataJob", jobRepository)
            .start(importPerfumeStep())
            .next(importPerfumeNoteStep())
            .next(importPerfumeAccordStep())
            .build()
    }

    @Bean
    fun importPerfumeStep(): Step {
        return StepBuilder("importPerfumeStep", jobRepository)
            .chunk<PerfumeCSV, Perfume>(100, transactionManager)
            .reader(perfumeReader(null))
            .processor(perfumeProcessor())
            .writer(perfumeWriter())
            .taskExecutor(taskExecutor)
            .build()
    }

    @Bean
    fun importPerfumeNoteStep(): Step {
        return StepBuilder("importPerfumeNoteStep", jobRepository)
            .chunk<PerfumeNoteCSV, PerfumeNote>(100, transactionManager)
            .reader(perfumeNoteReader(null))
            .processor(perfumeNoteProcessor())
            .writer(perfumeNoteWriter())
            .taskExecutor(taskExecutor)
            .build()
    }

    @Bean
    fun importPerfumeAccordStep(): Step {
        return StepBuilder("importPerfumeAccordStep", jobRepository)
            .chunk<PerfumeAccordCSV, PerfumeAccord>(100, transactionManager)
            .reader(perfumeAccordReader(null))
            .processor(perfumeAccordProcessor())
            .writer(perfumeAccordWriter())
            .taskExecutor(taskExecutor)
            .build()
    }

    @Bean
    @StepScope
    fun perfumeReader(@Value("#{jobParameters['perfumeFile']}") perfumeFile: String?): ItemReader<PerfumeCSV> {
        val file = perfumeFile ?: "data/perfume.csv"
        val headers = arrayOf(
            "id", "url", "title", "brand_id", "gender", "rating_value", "rating_count", 
            "year", "perfumer1", "perfumer2", "description", "updated_at"
        )
        return CSVItemReaderFactory.createReader(file, headers)
    }

    @Bean
    @StepScope
    fun perfumeNoteReader(@Value("#{jobParameters['noteFile']}") noteFile: String?): ItemReader<PerfumeNoteCSV> {
        val file = noteFile ?: "data/note.csv"
        val headers = arrayOf("id", "perfume_id", "note_type", "note_name")
        return CSVItemReaderFactory.createReader(file, headers)
    }

    @Bean
    @StepScope
    fun perfumeAccordReader(@Value("#{jobParameters['accordFile']}") accordFile: String?): ItemReader<PerfumeAccordCSV> {
        val file = accordFile ?: "data/main_accord.csv"
        val headers = arrayOf("id", "perfume_id", "accord_name", "position")
        return CSVItemReaderFactory.createReader(file, headers)
    }

    @Bean
    fun perfumeProcessor(): ItemProcessor<PerfumeCSV, Perfume> {
        return ItemProcessor { perfumeCSV ->
            // 브랜드가 존재하는 경우에만 처리
            val brandOptional = brandRepository.findById(perfumeCSV.brandId)
            if (brandOptional.isEmpty) {
                null
            } else {
                // 기존 향수가 있는지 확인
                val existingPerfume = perfumeRepository.findById(perfumeCSV.id)
                if (existingPerfume.isPresent) {
                    // 이미 존재하면 업데이트
                    val perfume = existingPerfume.get()
                    perfume.updateDetails(
                        name = perfumeCSV.title,
                        brand = brandOptional.get(),
                        description = perfumeCSV.description,
                        releaseYear = perfumeCSV.year,
                        concentration = null
                    )
                    perfume
                } else {
                    // 새로 생성
                    val perfume = Perfume.create(
                        name = perfumeCSV.title,
                        brand = brandOptional.get(),
                        description = perfumeCSV.description,
                        releaseYear = perfumeCSV.year,
                        concentration = null,
                        image = null,
                        creator = null,
                        isAdmin = true,
                        searchSynced = false
                    )
                    
                    // 퍼퓨머 추가
                    addPerfumers(perfume, perfumeCSV.perfumer1, perfumeCSV.perfumer2)
                    
                    perfume
                }
            }
        }
    }

    private fun addPerfumers(perfume: Perfume, perfumer1: String?, perfumer2: String?) {
        // 첫 번째 퍼퓨머 추가
        if (!perfumer1.isNullOrBlank()) {
            val designer = findOrCreateDesigner(perfumer1)
            perfume.addDesigner(designer, DesignerRole.PERFUMER)
        }
        
        // 두 번째 퍼퓨머 추가
        if (!perfumer2.isNullOrBlank() && perfumer1 != perfumer2) {
            val designer = findOrCreateDesigner(perfumer2)
            perfume.addDesigner(designer, DesignerRole.PERFUMER)
        }
    }
    
    private fun findOrCreateDesigner(name: String): Designer {
        return designerRepository.findByNameIgnoreCase(name)
            ?: designerRepository.save(Designer(name = name))
    }

    @Bean
    fun perfumeNoteProcessor(): ItemProcessor<PerfumeNoteCSV, PerfumeNote> {
        return ItemProcessor { perfumeNoteCSV ->
            val perfumeOptional = perfumeRepository.findById(perfumeNoteCSV.perfumeId)
            if (perfumeOptional.isEmpty) {
                null
            } else {
                val perfume = perfumeOptional.get()
                
                // 노트 타입 변환
                val noteType = when (perfumeNoteCSV.noteType) {
                    "TOP" -> NoteType.TOP
                    "MIDDLE" -> NoteType.MIDDLE
                    "BASE" -> NoteType.BASE
                    else -> NoteType.TOP // 기본값
                }
                
                // 노트 조회 또는 생성
                val note = noteRepository.findByNameIgnoreCase(perfumeNoteCSV.noteName)
                    ?: noteRepository.save(Note(name = perfumeNoteCSV.noteName))
                
                // 중복 체크
                val existingNotes = perfumeNoteRepository.findAll()
                val existingNote = existingNotes.find { 
                    it.perfume.id == perfume.id && it.note.id == note.id 
                }
                
                if (existingNote != null) {
                    existingNote
                } else {
                    perfume.addNote(note, noteType)
                }
            }
        }
    }

    @Bean
    fun perfumeAccordProcessor(): ItemProcessor<PerfumeAccordCSV, PerfumeAccord> {
        return ItemProcessor { perfumeAccordCSV ->
            val perfumeOptional = perfumeRepository.findById(perfumeAccordCSV.perfumeId)
            if (perfumeOptional.isEmpty) {
                null
            } else {
                val perfume = perfumeOptional.get()
                
                // 어코드 조회 또는 생성
                val accord = accordRepository.findByNameIgnoreCase(perfumeAccordCSV.accordName)
                    ?: accordRepository.save(Accord(name = perfumeAccordCSV.accordName))
                
                // 중복 체크
                val existingAccords = perfumeAccordRepository.findAll()
                val existingAccord = existingAccords.find { 
                    it.perfume.id == perfume.id && it.accord.id == accord.id 
                }
                
                if (existingAccord != null) {
                    existingAccord
                } else {
                    perfume.addAccord(accord)
                }
            }
        }
    }

    @Bean
    fun perfumeWriter(): ItemWriter<Perfume> {
        return JpaItemWriterBuilder<Perfume>()
            .entityManagerFactory(entityManagerFactory)
            .build()
    }

    @Bean
    fun perfumeNoteWriter(): ItemWriter<PerfumeNote> {
        return JpaItemWriterBuilder<PerfumeNote>()
            .entityManagerFactory(entityManagerFactory)
            .build()
    }

    @Bean
    fun perfumeAccordWriter(): ItemWriter<PerfumeAccord> {
        return JpaItemWriterBuilder<PerfumeAccord>()
            .entityManagerFactory(entityManagerFactory)
            .build()
    }
} 