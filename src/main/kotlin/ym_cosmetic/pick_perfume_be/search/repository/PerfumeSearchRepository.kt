package ym_cosmetic.pick_perfume_be.search.repository

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument

interface PerfumeSearchRepository : ElasticsearchRepository<PerfumeDocument, String>,
    PerfumeSearchRepositoryCustom

