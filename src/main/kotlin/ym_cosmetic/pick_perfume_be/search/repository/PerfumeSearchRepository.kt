package ym_cosmetic.pick_perfume_be.search.repository

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument

interface PerfumeSearchRepository : ElasticsearchRepository<PerfumeDocument, String>,
    PerfumeSearchRepositoryCustom {
    fun findByBrandName(brandName: String): List<PerfumeDocument>
    fun findByNotesByTypeTypeAndNotesByTypeNotesContains(
        type: String,
        note: String
    ): List<PerfumeDocument>

    fun findByAccordsContaining(accord: String): List<PerfumeDocument>
}

