package ym_cosmetic.pick_perfume_be.perfume.service

import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ym_cosmetic.pick_perfume_be.accord.entity.Accord
import ym_cosmetic.pick_perfume_be.accord.repository.AccordRepository
import ym_cosmetic.pick_perfume_be.brand.entity.Brand
import ym_cosmetic.pick_perfume_be.brand.repository.BrandRepository
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.designer.entity.Designer
import ym_cosmetic.pick_perfume_be.designer.repository.DesignerRepository
import ym_cosmetic.pick_perfume_be.infrastructure.s3.S3Service
import ym_cosmetic.pick_perfume_be.infrastructure.gemini.GeminiImageService
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.enums.MemberRole
import ym_cosmetic.pick_perfume_be.member.repository.MemberRepository
import ym_cosmetic.pick_perfume_be.note.entity.Note
import ym_cosmetic.pick_perfume_be.note.repository.NoteRepository
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeCreateRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeDesignerRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeFilterRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.request.PerfumeUpdateRequest
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumePageResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeAiImageResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryResponse
import ym_cosmetic.pick_perfume_be.perfume.dto.response.PerfumeSummaryStats
import ym_cosmetic.pick_perfume_be.perfume.entity.Perfume
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeLike
import ym_cosmetic.pick_perfume_be.perfume.entity.PerfumeView
import ym_cosmetic.pick_perfume_be.perfume.repository.*
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType
import ym_cosmetic.pick_perfume_be.search.event.PerfumeCreatedEvent
import ym_cosmetic.pick_perfume_be.search.event.PerfumeDeletedEvent
import ym_cosmetic.pick_perfume_be.search.event.PerfumeUpdatedEvent
import java.time.LocalDate

@Service
class PerfumeService(
    private val perfumeRepository: PerfumeRepository,
    private val noteRepository: NoteRepository,
    private val accordRepository: AccordRepository,
    private val brandRepository: BrandRepository,
    private val designerRepository: DesignerRepository,
    private val perfumeNoteRepository: PerfumeNoteRepository,
    private val perfumeAccordRepository: PerfumeAccordRepository,
    private val perfumeDesignerRepository: PerfumeDesignerRepository,
    private val memberRepository: MemberRepository,
    private val s3Service: S3Service,
    private val eventPublisher: ApplicationEventPublisher,
    private val perfumeLikeRepository: PerfumeLikeRepository,
    private val perfumeViewRepository: PerfumeViewRepository,
    private val geminiImageService: GeminiImageService
) {
    @Transactional(readOnly = true)
    fun findPerfumeById(id: Long, member: Member?): PerfumeResponse {
        val perfume = perfumeRepository.findByIdWithCreatorAndBrand(id)
            ?: throw EntityNotFoundException("Perfume not found with id: $id")

        val isLiked = checkIfPerfumeLikedByMember(id, member)
        val likeCount = getPerfumeLikeCount(id)
        val viewCount = getPerfumeViewCount(id)

        val aiPreviewImage = generatePerfumeAiPreview(perfume)

        return PerfumeResponse.from(perfume, isLiked, likeCount, viewCount, aiPreviewImage)
    }

    /**
     * 향수 상세 조회와 함께 조회수 증가
     * 같은 사용자(또는 IP)가 같은 날에 여러 번 조회해도 조회수는 1번만 증가합니다
     */
    @Transactional
    fun findPerfumeByIdAndIncreaseViewCount(
        id: Long,
        member: Member?,
        request: HttpServletRequest
    ): PerfumeResponse {
        val perfume = perfumeRepository.findByIdWithCreatorAndBrand(id)
            ?: throw EntityNotFoundException("Perfume not found with id: $id")

        // 조회수 처리 - 같은 날에 같은 사용자(또는 IP)의 중복 조회 방지
        val today = LocalDate.now()
        val ipAddress = extractClientIp(request)

        if (member != null && member.id != null) {
            // 로그인 사용자의 경우 회원 ID로 확인
            val alreadyViewed = perfumeViewRepository.existsByPerfumeIdAndMemberIdAndViewDate(
                id, member.id!!, today
            )

            if (!alreadyViewed) {
                perfumeViewRepository.save(PerfumeView.create(perfume, member, ipAddress))
            }
        } else if (ipAddress != null) {
            // 비로그인 사용자의 경우 IP 주소로 확인
            val alreadyViewed = perfumeViewRepository.existsByPerfumeIdAndIpAddressAndViewDate(
                id, ipAddress, today
            )

            if (!alreadyViewed) {
                perfumeViewRepository.save(PerfumeView.create(perfume, null, ipAddress))
            }
        }

        val isLiked = checkIfPerfumeLikedByMember(id, member)
        val likeCount = getPerfumeLikeCount(id)
        val viewCount = getPerfumeViewCount(id)

        val aiPreviewImage = generatePerfumeAiPreview(perfume)

        return PerfumeResponse.from(perfume, isLiked, likeCount, viewCount, aiPreviewImage)
    }


    @Transactional(readOnly = true)
    fun findAllApprovedPerfumes(pageable: Pageable, member: Member?): Page<PerfumeSummaryResponse> {
        val perfumePage = perfumeRepository.findAllApprovedWithCreatorAndBrand(pageable)
        val likedPerfumeIds = getLikedPerfumeIdsByMember(member)

        // 모든 향수 ID에 대한 좋아요 카운트를 조회
        val perfumeIds = perfumePage.content.map { it.id!! }
        val likeCounts = perfumeIds.associateWith { getPerfumeLikeCount(it) }
        val viewCounts = perfumeIds.associateWith { getPerfumeViewCount(it) }

        return perfumePage.map { perfume ->
            PerfumeSummaryResponse.from(
                perfume = perfume,
                isLiked = likedPerfumeIds.contains(perfume.id),
                likeCount = likeCounts[perfume.id] ?: 0,
                viewCount = viewCounts[perfume.id] ?: 0
            )
        }
    }

    @Transactional(readOnly = true)
    fun findAllPerfumesWithFilter(
        filter: PerfumeFilterRequest?,
        pageable: Pageable,
        member: Member?,
        includeStats: Boolean = false
    ): PerfumePageResponse {
        // 필터가 없는 경우 기본 조회
        val perfumePage = if (filter == null) {
            perfumeRepository.findAllApprovedWithCreatorAndBrand(pageable)
        } else {
            perfumeRepository.findAllApprovedWithFilter(filter, pageable)
        }

        val likedPerfumeIds = getLikedPerfumeIdsByMember(member)

        // 모든 향수 ID에 대한 좋아요 카운트와 조회수를 조회
        val perfumeIds = perfumePage.content.map { it.id!! }
        val likeCounts = perfumeIds.associateWith { getPerfumeLikeCount(it) }
        val viewCounts = perfumeIds.associateWith { getPerfumeViewCount(it) }

        val perfumesPage = perfumePage.map { perfume ->
            PerfumeSummaryResponse.from(
                perfume = perfume,
                isLiked = likedPerfumeIds.contains(perfume.id),
                likeCount = likeCounts[perfume.id] ?: 0,
                viewCount = viewCounts[perfume.id] ?: 0
            )
        }

        // 통계 정보 포함 여부에 따라 반환
        val stats = if (includeStats) {
            getPerfumeStatistics()
        } else {
            null
        }

        return PerfumePageResponse(
            perfumes = perfumesPage,
            stats = stats
        )
    }

    @Transactional(readOnly = true)
    fun getPerfumeStatistics(): PerfumeSummaryStats {
        val brandStats = perfumeRepository.findTopBrandStats(20)
        val genderStats = perfumeRepository.findGenderStats()
        val accordStats = perfumeRepository.findTopAccordStats(20)

        return PerfumeSummaryStats(
            brandStats = brandStats,
            genderStats = genderStats,
            accordStats = accordStats
        )
    }

    @Transactional
    fun createPerfume(request: PerfumeCreateRequest, memberId: Long?): PerfumeResponse {
        val creator = memberId?.let {
            memberRepository.findById(it).orElse(null)
        }

        // 브랜드 조회 또는 생성
        val brand = getBrandByNameOrCreate(request.brandName)

        val perfume = Perfume.create(
            name = request.name,
            brand = brand,
            gender = request.gender,
            content = request.content,
            releaseYear = request.releaseYear,
            concentration = request.concentration,
            image = request.imageUrl?.let { ImageUrl(it) },
            creator = creator,
            isAdmin = creator?.memberRole == MemberRole.ADMIN
        )

        val savedPerfume = perfumeRepository.save(perfume)

        // 노트 추가
        addPerfumeNotes(savedPerfume, request.topNotes, NoteType.TOP)
        addPerfumeNotes(savedPerfume, request.middleNotes, NoteType.MIDDLE)
        addPerfumeNotes(savedPerfume, request.baseNotes, NoteType.BASE)

        // 어코드 추가
        addPerfumeAccords(savedPerfume, request.accords)

        // 디자이너 추가
        addPerfumeDesigners(savedPerfume, request.designers)
        eventPublisher.publishEvent(PerfumeCreatedEvent(savedPerfume.id!!))

        return PerfumeResponse.from(savedPerfume)
    }

    @Transactional
    fun updatePerfume(id: Long, request: PerfumeUpdateRequest): PerfumeResponse {
        val perfume = perfumeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Perfume not found with id: $id") }

        // 브랜드 업데이트
        val brand = getBrandByNameOrCreate(request.brandName)

        perfume.updateDetails(
            name = request.name,
            brand = brand,
            content = request.content,
            releaseYear = request.releaseYear,
            concentration = request.concentration
        )

        request.imageUrl?.let {
            perfume.updateImage(ImageUrl(it))
        }

        // 노트 업데이트
        updatePerfumeNotes(perfume, request.topNotes, NoteType.TOP)
        updatePerfumeNotes(perfume, request.middleNotes, NoteType.MIDDLE)
        updatePerfumeNotes(perfume, request.baseNotes, NoteType.BASE)

        // 어코드 업데이트
        updatePerfumeAccords(perfume, request.accords)

        // 디자이너 업데이트
        updatePerfumeDesigners(perfume, request.designers)
        eventPublisher.publishEvent(PerfumeUpdatedEvent(id))

        return PerfumeResponse.from(perfume)
    }

    @Transactional
    fun uploadPerfumeImage(id: Long, file: MultipartFile): String {
        val perfume = perfumeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Perfume not found with id: $id") }

        val imageUrl = s3Service.uploadFile("perfumes/${id}", file)
        perfume.updateImage(ImageUrl(imageUrl))

        return imageUrl
    }

    @Transactional
    fun approvePerfume(id: Long): PerfumeResponse {
        val perfume = perfumeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Perfume not found with id: $id") }

        perfume.approve()

        return PerfumeResponse.from(perfume)
    }

    @Transactional
    fun deletePerfume(id: Long) {
        // 관련 데이터 삭제
        perfumeNoteRepository.deleteByPerfumeId(id)
        perfumeAccordRepository.deleteByPerfumeId(id)
        perfumeDesignerRepository.deleteByPerfumeId(id)

        // 향수 삭제
        perfumeRepository.deleteById(id)
        eventPublisher.publishEvent(PerfumeDeletedEvent(id))
    }

    private fun checkIfPerfumeLikedByMember(perfumeId: Long, member: Member?): Boolean {
        if (member == null || member.id == null) {
            return false
        }

        return perfumeLikeRepository.existsByPerfumeIdAndMemberId(perfumeId, member.id!!)
    }

    private fun getLikedPerfumeIdsByMember(member: Member?): Set<Long> {
        if (member == null || member.id == null) {
            return emptySet()
        }

        return perfumeLikeRepository.findPerfumeIdsByMemberId(member.id!!)
    }

    private fun generatePerfumeAiPreview(perfume: Perfume): PerfumeAiImageResponse? {
        val prompt = buildPerfumeImagePrompt(perfume)
        if (prompt.isBlank()) {
            return null
        }

        return geminiImageService.generateImage(
            prompt = prompt,
            referenceImageUrl = perfume.image?.url
        )?.let { generated ->
            PerfumeAiImageResponse(
                mimeType = generated.mimeType,
                data = generated.data
            )
        }
    }

    private fun buildPerfumeImagePrompt(perfume: Perfume): String {
        val accords = perfume.getAccords()
            .sortedBy { it.position ?: Int.MAX_VALUE }
            .map { it.accord.name }
        val mainAccord = accords.firstOrNull()
        val supportingAccords = accords.drop(1)

        val topNotes = perfume.getNotesByType(NoteType.TOP).map { it.note.name }
        val middleNotes = perfume.getNotesByType(NoteType.MIDDLE).map { it.note.name }
        val baseNotes = perfume.getNotesByType(NoteType.BASE).map { it.note.name }

        return buildString {
            appendLine("Create a single, high-quality concept image that conveys the mood of this perfume. The image should help users imagine the scent profile. Avoid any text overlays.")
            appendLine("Perfume: ${perfume.name}")
            appendLine("Brand: ${perfume.brand.name}")
            mainAccord?.let { appendLine("Primary accord: $it") }
            if (supportingAccords.isNotEmpty()) {
                appendLine("Supporting accords: ${supportingAccords.joinToString()}")
            }
            if (topNotes.isNotEmpty()) {
                appendLine("Top notes: ${topNotes.joinToString()}")
            }
            if (middleNotes.isNotEmpty()) {
                appendLine("Heart notes: ${middleNotes.joinToString()}")
            }
            if (baseNotes.isNotEmpty()) {
                appendLine("Base notes: ${baseNotes.joinToString()}")
            }
            perfume.content?.takeIf { it.isNotBlank() }?.let {
                appendLine("Narrative inspiration: ${it.trim()}")
            }
            append("Style guidance: focus on atmosphere, lighting, and textures that fit the accords. Make it feel immersive without showing product packaging.")
        }.trim()
    }

    private fun getPerfumeLikeCount(perfumeId: Long): Int {
        return perfumeLikeRepository.countByPerfumeId(perfumeId)
    }

    private fun getPerfumeViewCount(perfumeId: Long): Int {
        return perfumeViewRepository.countByPerfumeId(perfumeId)
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private fun extractClientIp(request: HttpServletRequest): String? {
        var ip = request.getHeader("X-Forwarded-For")

        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_CLIENT_IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.remoteAddr
        }

        // 여러 프록시를 거친 경우 첫 번째 IP만 사용
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim()
        }

        return ip
    }

    private fun getBrandByNameOrCreate(brandName: String): Brand {
        return brandRepository.findByNameIgnoreCase(brandName)
            ?: brandRepository.save(Brand(name = brandName))
    }

    private fun getDesignerByNameOrCreate(designerName: String): Designer {
        return designerRepository.findByNameIgnoreCase(designerName)
            ?: designerRepository.save(Designer(name = designerName))
    }

    private fun addPerfumeNotes(perfume: Perfume, noteNames: List<String>, type: NoteType) {
        noteNames.forEach { noteName ->
            val note = noteRepository.findByNameIgnoreCase(noteName)
                ?: noteRepository.save(Note(name = noteName))

            val perfumeNote = perfume.addNote(note, type)
            perfumeNoteRepository.save(perfumeNote)
        }
    }

    private fun addPerfumeAccords(perfume: Perfume, accordNames: List<String>) {
        accordNames.forEach { accordName ->
            val accord = accordRepository.findByNameIgnoreCase(accordName)
                ?: accordRepository.save(Accord(name = accordName))

            val perfumeAccord = perfume.addAccord(accord)
            perfumeAccordRepository.save(perfumeAccord)
        }
    }

    private fun addPerfumeDesigners(perfume: Perfume, designers: List<PerfumeDesignerRequest>) {
        designers.forEach { designerRequest ->
            val designer = getDesignerByNameOrCreate(designerRequest.designerName)
            val perfumeDesigner =
                perfume.addDesigner(designer, designerRequest.role, designerRequest.content)
            perfumeDesignerRepository.save(perfumeDesigner)
        }
    }

    private fun updatePerfumeNotes(perfume: Perfume, noteNames: List<String>, type: NoteType) {
        // 기존 노트 삭제
        perfumeNoteRepository.deleteByPerfumeIdAndType(perfume.id!!, type)

        // 새 노트 추가
        addPerfumeNotes(perfume, noteNames, type)
    }

    private fun updatePerfumeAccords(perfume: Perfume, accordNames: List<String>) {
        // 기존 어코드 삭제
        perfumeAccordRepository.deleteByPerfumeId(perfume.id!!)

        // 새 어코드 추가
        addPerfumeAccords(perfume, accordNames)
    }

    private fun updatePerfumeDesigners(perfume: Perfume, designers: List<PerfumeDesignerRequest>) {
        // 기존 디자이너 삭제
        perfumeDesignerRepository.deleteByPerfumeId(perfume.id!!)

        // 새 디자이너 추가
        addPerfumeDesigners(perfume, designers)
    }

    @Transactional
    fun likePerfume(perfumeId: Long, member: Member): Boolean {
        if (member.id == null) {
            throw IllegalArgumentException("인증된 사용자만 좋아요를 할 수 있습니다.")
        }

        val perfume = perfumeRepository.findById(perfumeId)
            .orElseThrow { EntityNotFoundException("해당 향수를 찾을 수 없습니다: $perfumeId") }

        if (perfumeLikeRepository.existsByPerfumeIdAndMemberId(perfumeId, member.id!!)) {
            return true // 이미 좋아요 상태
        }

        val perfumeLike = PerfumeLike.create(perfume, member)
        perfumeLikeRepository.save(perfumeLike)

        return true
    }

    @Transactional
    fun unlikePerfume(perfumeId: Long, member: Member): Boolean {
        if (member.id == null) {
            throw IllegalArgumentException("인증된 사용자만 좋아요 취소를 할 수 있습니다.")
        }

        perfumeLikeRepository.findByPerfumeIdAndMemberId(perfumeId, member.id!!)?.let {
            perfumeLikeRepository.delete(it)
        }

        return false
    }

    @Transactional(readOnly = true)
    fun findLikedPerfumes(member: Member, pageable: Pageable): Page<PerfumeSummaryResponse> {
        if (member.id == null) {
            throw IllegalArgumentException("인증된 사용자만 좋아요한 향수 목록을 조회할 수 있습니다.")
        }

        // 사용자가 좋아요한 향수 ID 목록 조회
        val likedPerfumeIds = perfumeLikeRepository.findPerfumeIdsByMemberId(member.id!!)
        if (likedPerfumeIds.isEmpty()) {
            return Page.empty(pageable)
        }

        // 좋아요한 향수 목록 조회
        val perfumePage = perfumeRepository.findByIdIn(likedPerfumeIds.toList(), pageable)

        // 좋아요 카운트와 조회수 카운트 조회
        val perfumeIds = perfumePage.content.map { it.id!! }
        val likeCounts = perfumeIds.associateWith { getPerfumeLikeCount(it) }
        val viewCounts = perfumeIds.associateWith { getPerfumeViewCount(it) }

        return perfumePage.map { perfume ->
            PerfumeSummaryResponse.from(
                perfume = perfume,
                isLiked = true, // 본인이 좋아요한 목록이므로 항상 true
                likeCount = likeCounts[perfume.id] ?: 0,
                viewCount = viewCounts[perfume.id] ?: 0
            )
        }
    }

    @Transactional(readOnly = true)
    fun findViewedPerfumes(member: Member, pageable: Pageable): Page<PerfumeSummaryResponse> {
        if (member.id == null) {
            throw IllegalArgumentException("인증된 사용자만 조회한 향수 목록을 조회할 수 있습니다.")
        }

        // 사용자가 조회한 향수 ID 목록 조회
        val viewedPerfumeIds = perfumeViewRepository.findPerfumeIdsByMemberId(member.id!!)
        if (viewedPerfumeIds.isEmpty()) {
            return Page.empty(pageable)
        }

        // 조회한 향수 목록 조회
        val perfumePage = perfumeRepository.findByIdIn(viewedPerfumeIds, pageable)

        // 사용자가 좋아요한 향수 ID 목록 조회
        val likedPerfumeIds = perfumeLikeRepository.findPerfumeIdsByMemberId(member.id!!)

        // 좋아요 카운트와 조회수 카운트 조회
        val perfumeIds = perfumePage.content.map { it.id!! }
        val likeCounts = perfumeIds.associateWith { getPerfumeLikeCount(it) }
        val viewCounts = perfumeIds.associateWith { getPerfumeViewCount(it) }

        return perfumePage.map { perfume ->
            PerfumeSummaryResponse.from(
                perfume = perfume,
                isLiked = likedPerfumeIds.contains(perfume.id),
                likeCount = likeCounts[perfume.id] ?: 0,
                viewCount = viewCounts[perfume.id] ?: 0
            )
        }
    }
}





