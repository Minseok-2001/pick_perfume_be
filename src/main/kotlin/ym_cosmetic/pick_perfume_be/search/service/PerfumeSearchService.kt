package ym_cosmetic.pick_perfume_be.search.service

import org.apache.lucene.search.join.ScoreMode
import org.opensearch.data.client.orhlc.NativeSearchQueryBuilder
import org.opensearch.data.core.OpenSearchOperations
import org.opensearch.index.query.QueryBuilders
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Service
import ym_cosmetic.pick_perfume_be.member.dto.MemberPreferenceDto
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchCriteria
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchPageResult
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchResult

@Service
class PerfumeSearchService(
    private val openSearchOperations: OpenSearchOperations
) {
    fun searchPerfumes(criteria: PerfumeSearchCriteria): PerfumeSearchPageResult {
        // OpenSearch QueryBuilders를 사용하여 쿼리 생성
        val boolQueryBuilder = QueryBuilders.boolQuery()

        // 키워드 검색
        criteria.keyword?.let { keyword ->
            val keywordBoolQuery = QueryBuilders.boolQuery()
            
            keywordBoolQuery.should(QueryBuilders.matchQuery("name", keyword).boost(2.0f))
            keywordBoolQuery.should(QueryBuilders.matchQuery("content", keyword))
            keywordBoolQuery.should(QueryBuilders.matchQuery("brandName.text", keyword).boost(3.0f))
            keywordBoolQuery.should(QueryBuilders.matchQuery("notes.text", keyword))
            keywordBoolQuery.should(QueryBuilders.matchQuery("accords.text", keyword))
            keywordBoolQuery.minimumShouldMatch(1)
            
            boolQueryBuilder.must(keywordBoolQuery)
        }

        // 브랜드 필터
        criteria.brandName?.let { brand ->
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandName.keyword", brand))
        }

        // 노트 필터
        criteria.note?.let { note ->
            boolQueryBuilder.filter(QueryBuilders.termQuery("notes.keyword", note))
        }

        // 계절 필터
        criteria.season?.let { season ->
            boolQueryBuilder.filter(QueryBuilders.termQuery("season.keyword", season))
        }

        // 성별 필터 추가
        criteria.gender?.let { gender ->
            boolQueryBuilder.filter(QueryBuilders.termQuery("gender.keyword", gender))
        }

        // 노트 타입별 필터
        if (criteria.noteType != null && criteria.note != null) {
            val nestedQuery = QueryBuilders.nestedQuery(
                "notesByType",
                QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("notesByType.type", criteria.noteType.name))
                    .must(QueryBuilders.termQuery("notesByType.notes.keyword", criteria.note)),
                ScoreMode.None
            )
            boolQueryBuilder.filter(nestedQuery)
        }

        // 어코드 필터
        criteria.accord?.let { accord ->
            boolQueryBuilder.filter(QueryBuilders.termQuery("accords.keyword", accord))
        }

        // 출시 연도 범위 필터
        if (criteria.fromYear != null || criteria.toYear != null) {
            val rangeQueryBuilder = QueryBuilders.rangeQuery("releaseYear")
            if (criteria.fromYear != null) rangeQueryBuilder.gte(criteria.fromYear)
            if (criteria.toYear != null) rangeQueryBuilder.lte(criteria.toYear)
            boolQueryBuilder.filter(rangeQueryBuilder)
        }
        
        // 평점 범위 필터
        if (criteria.minRating != null || criteria.maxRating != null) {
            val rangeQueryBuilder = QueryBuilders.rangeQuery("averageRating")
            if (criteria.minRating != null) rangeQueryBuilder.gte(criteria.minRating)
            if (criteria.maxRating != null) rangeQueryBuilder.lte(criteria.maxRating)
            boolQueryBuilder.filter(rangeQueryBuilder)
        }

        // 쿼리 생성
        val nativeQueryBuilder = NativeSearchQueryBuilder()
            .withQuery(boolQueryBuilder)
            .withPageable(criteria.pageable)

        // 정렬 조건 추가
        when (criteria.sortBy) {
            "newest" -> nativeQueryBuilder.withSort(
                org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "releaseYear"
                )
            )
            "rating" -> nativeQueryBuilder.withSort(
                org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "averageRating"
                )
            )
            "popularity" -> nativeQueryBuilder.withSort(
                org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "reviewCount"
                )
            )
            else -> nativeQueryBuilder.withSort(
                org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "_score"
                )
            )
        }

        // 검색 실행
        val searchHits: SearchHits<PerfumeDocument> = openSearchOperations.search(
            nativeQueryBuilder.build(),
            PerfumeDocument::class.java,
            IndexCoordinates.of("perfumes")
        )

        // 결과 변환
        val content = searchHits.map { hit ->
            PerfumeSearchResult.fromDocument(hit.content)
        }.toList()

        // 페이지네이션 정보를 포함한 결과 반환
        return PerfumeSearchPageResult.of(
            content = content,
            totalCount = searchHits.totalHits,
            page = criteria.pageable.pageNumber,
            size = criteria.pageable.pageSize
        )
    }

    // 유사한 향수 찾기
    fun findSimilarPerfumes(perfumeId: String, limit: Int): List<PerfumeSearchResult> {
        // 향수 문서 가져오기
        val perfume = openSearchOperations.get(perfumeId, PerfumeDocument::class.java)
            ?: return emptyList()

        // 유사성 쿼리 구성
        val boolQueryBuilder = QueryBuilders.boolQuery()

        // 동일한 향수 제외
        boolQueryBuilder.mustNot(QueryBuilders.idsQuery().addIds(perfumeId))

        // 노트 유사성 (가중치 높음)
        val notesQuery = QueryBuilders.boolQuery()
        perfume.notes.forEach { note ->
            notesQuery.should(
                QueryBuilders.termQuery("notes.name.keyword", note.name).boost(2.0f)
            )
        }
        boolQueryBuilder.should(notesQuery)

        // 어코드 유사성
        val accordsQuery = QueryBuilders.boolQuery()
        perfume.accords.forEach { accord ->
            accordsQuery.should(
                QueryBuilders.termQuery("accords.name.keyword", accord.name).boost(1.5f)
            )
        }
        boolQueryBuilder.should(accordsQuery)

        // 동일 브랜드 (약간의 가중치)
        boolQueryBuilder.should(
            QueryBuilders.termQuery("brandName.keyword", perfume.brandName).boost(0.8f)
        )

        // 농도 유사성
        perfume.concentration?.let { concentration ->
            boolQueryBuilder.should(
                QueryBuilders.termQuery("concentration.keyword", concentration).boost(0.5f)
            )
        }

        // 최소 매치 설정 (최소한 하나의 유사성은 있어야 함)
        boolQueryBuilder.minimumShouldMatch(1)

        // 쿼리 실행
        val nativeQuery = NativeSearchQueryBuilder()
            .withQuery(boolQueryBuilder)
            .withPageable(PageRequest.of(0, limit))
            .build()

        val searchHits = openSearchOperations.search(
            nativeQuery,
            PerfumeDocument::class.java,
            IndexCoordinates.of("perfumes")
        )

        // 결과 변환
        val content = searchHits.map { hit ->
            PerfumeSearchResult.fromDocument(hit.content)
        }.toList()

        return content
    }

    // 사용자 선호도 기반 추천
    fun findRecommendedPerfumes(
        memberPreferences: MemberPreferenceDto,
        limit: Int
    ): List<PerfumeSearchResult> {
        val boolQueryBuilder = QueryBuilders.boolQuery()

        // 사용자가 이미 리뷰한 향수 제외
        if (memberPreferences.reviewedPerfumeIds.isNotEmpty()) {
            boolQueryBuilder.mustNot(
                QueryBuilders.idsQuery().addIds(*memberPreferences.reviewedPerfumeIds.map { it.toString() }.toTypedArray())
            )
        }

        // 선호 노트 기반 추천
        if (memberPreferences.preferredNotes.isNotEmpty()) {
            val notesQuery = QueryBuilders.boolQuery()
            memberPreferences.preferredNotes.forEach { note ->
                notesQuery.should(
                    QueryBuilders.termQuery("notes.keyword", note).boost(2.0f)
                )
            }
            boolQueryBuilder.should(notesQuery)
        }

        // 선호 어코드 기반 추천
        if (memberPreferences.preferredAccords.isNotEmpty()) {
            val accordsQuery = QueryBuilders.boolQuery()
            memberPreferences.preferredAccords.forEach { accord ->
                accordsQuery.should(
                    QueryBuilders.termQuery("accords.keyword", accord).boost(1.5f)
                )
            }
            boolQueryBuilder.should(accordsQuery)
        }

        // 선호 브랜드 기반 추천
        if (memberPreferences.preferredBrands.isNotEmpty()) {
            val brandsQuery = QueryBuilders.boolQuery()
            memberPreferences.preferredBrands.forEach { brand ->
                brandsQuery.should(
                    QueryBuilders.termQuery("brandName.keyword", brand).boost(1.0f)
                )
            }
            boolQueryBuilder.should(brandsQuery)
        }

        // 최소 하나 이상 일치해야 함
        if (memberPreferences.preferredNotes.isNotEmpty() ||
            memberPreferences.preferredAccords.isNotEmpty() ||
            memberPreferences.preferredBrands.isNotEmpty()
        ) {
            boolQueryBuilder.minimumShouldMatch(1)
        }

        // 쿼리 실행
        val nativeQuery = NativeSearchQueryBuilder()
            .withQuery(boolQueryBuilder)
            .withPageable(PageRequest.of(0, limit))
            .build()

        val searchHits = openSearchOperations.search(
            nativeQuery,
            PerfumeDocument::class.java,
            IndexCoordinates.of("perfumes")
        )

        // 결과 변환
        val content = searchHits.map { hit ->
            PerfumeSearchResult.fromDocument(hit.content)
        }.toList()

        return content
    }
}