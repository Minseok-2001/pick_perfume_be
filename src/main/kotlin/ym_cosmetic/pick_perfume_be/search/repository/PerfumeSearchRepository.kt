package ym_cosmetic.pick_perfume_be.search.repository

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import ym_cosmetic.pick_perfume_be.member.entity.MemberPreference
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchCriteria

interface PerfumeSearchRepository : ElasticsearchRepository<PerfumeDocument, String>,
    PerfumeSearchRepositoryCustom {
    fun findByBrandName(brandName: String): List<PerfumeDocument>
    fun findByNotesByTypeTypeAndNotesByTypeNotesContains(
        type: String,
        note: String
    ): List<PerfumeDocument>

    fun findByAccordsContaining(accord: String): List<PerfumeDocument>
}

interface PerfumeSearchRepositoryCustom {
    fun search(searchCriteria: PerfumeSearchCriteria): List<PerfumeDocument>
    fun findSimilarPerfumes(perfumeId: String, limit: Int): List<PerfumeDocument>
    fun findRecommendedPerfumesByMemberPreferences(
        memberPreference: MemberPreference,
        limit: Int
    ): List<PerfumeDocument>
}