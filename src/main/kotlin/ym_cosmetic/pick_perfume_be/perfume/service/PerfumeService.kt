package ym_cosmetic.pick_perfume_be.perfume.service

import ym_cosmetic.pick_perfume_be.perfume.repository.PerfumeRepository

package com.fragrantica.domain.perfume.service

import com.fragrantica.domain.accord.entity.Accord
import com.fragrantica.domain.accord.repository.AccordRepository
import com.fragrantica.domain.common.vo.ImageUrl
import com.fragrantica.domain.common.vo.NoteType
import com.fragrantica.domain.member.entity.Member
import com.fragrantica.domain.note.entity.Note
import com.fragrantica.domain.note.repository.NoteRepository
import com.fragrantica.domain.perfume.dto.PerfumeCreateDto
import com.fragrantica.domain.perfume.entity.Perfume
import com.fragrantica.domain.perfume.entity.PerfumeAccord
import com.fragrantica.domain.perfume.entity.PerfumeNote
import com.fragrantica.domain.perfume.repository.PerfumeAccordRepository
import com.fragrantica.domain.perfume.repository.PerfumeNoteRepository
import com.fragrantica.domain.perfume.repository.PerfumeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PerfumeService(
    private val perfumeRepository: PerfumeRepository,
    private val noteRepository: NoteRepository,
    private val accordRepository: AccordRepository,
    private val perfumeNoteRepository: PerfumeNoteRepository,
    private val perfumeAccordRepository: PerfumeAccordRepository
) {
    @Transactional
    fun createPerfume(dto: PerfumeCreateDto, creator: Member?): Perfume {
        // 향수 생성
        val perfume = Perfume(
            name = dto.name,
            brand = dto.brand,
            description = dto.description,
            releaseYear = dto.releaseYear,
            perfumer = dto.perfumer,
            concentration = dto.concentration,
            image = dto.imageUrl?.let { ImageUrl(it) },
            creator = creator
        )

        val savedPerfume = perfumeRepository.save(perfume)

        // 노트 추가 (존재하는지 확인하고 없으면 생성)
        addPerfumeNotes(savedPerfume, dto.topNotes, NoteType.TOP)
        addPerfumeNotes(savedPerfume, dto.middleNotes, NoteType.MIDDLE)
        addPerfumeNotes(savedPerfume, dto.baseNotes, NoteType.BASE)

        // 어코드 추가 (존재하는지 확인하고 없으면 생성)
        addPerfumeAccords(savedPerfume, dto.accordNames)

        return savedPerfume
    }

    private fun addPerfumeNotes(perfume: Perfume, noteNames: List<String>, type: NoteType) {
        noteNames.forEach { noteName ->
            val note = noteRepository.findByNameIgnoreCase(noteName)
                ?: noteRepository.save(Note(name = noteName))

            val perfumeNote = PerfumeNote(
                perfume = perfume,
                note = note,
                type = type
            )
            perfumeNoteRepository.save(perfumeNote)
        }
    }

    private fun addPerfumeAccords(perfume: Perfume, accordNames: List<String>) {
        accordNames.forEach { accordName ->
            val accord = accordRepository.findByNameIgnoreCase(accordName)
                ?: accordRepository.save(Accord(name = accordName))

            val perfumeAccord = PerfumeAccord(
                perfume = perfume,
                accord = accord
            )
            perfumeAccordRepository.save(perfumeAccord)
        }
    }

    @Transactional
    fun deletePerfume(id: Long) {
        // 논리적 FK를 사용할 경우 참조 무결성을 직접 관리해야 함
        // 먼저 관련 데이터를 삭제
        perfumeNoteRepository.deleteByPerfumeId(id)
        perfumeAccordRepository.deleteByPerfumeId(id)
        // 추가로 리뷰, 투표 등 관련 데이터 삭제

        // 마지막에 향수 자체를 삭제
        perfumeRepository.deleteById(id)
    }
}