package ym_cosmetic.pick_perfume_be.search.repository

import ym_cosmetic.pick_perfume_be.member.entity.MemberPreference
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchCriteria

interface PerfumeSearchRepositoryCustom {
    fun search(searchCriteria: PerfumeSearchCriteria): List<PerfumeDocument>
    fun findSimilarPerfumes(perfumeId: String, limit: Int): List<PerfumeDocument>
    fun findRecommendedPerfumesByMemberPreferences(
        memberPreference: MemberPreference,
        limit: Int
    ): List<PerfumeDocument>
}