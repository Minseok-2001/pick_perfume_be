package ym_cosmetic.pick_perfume_be.search.service

import co.elastic.clients.elasticsearch._types.ScoreSort
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Service
import ym_cosmetic.pick_perfume_be.member.dto.MemberPreferenceDto
import ym_cosmetic.pick_perfume_be.member.entity.QMemberPreference.memberPreference
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchCriteria
import ym_cosmetic.pick_perfume_be.search.dto.PerfumeSearchResult

@Service
class PerfumeSearchService(
    private val elasticsearchOperations: ElasticsearchOperations
) {
    fun searchPerfumes(criteria: PerfumeSearchCriteria): List<PerfumeSearchResult> {
        val boolQuery = BoolQuery.Builder()

        // 키워드 검색
        criteria.keyword?.let { keyword ->
            val keywordQuery = BoolQuery.Builder()
                .should(
                    Query.of { q -> q.match { m -> m.field("name").query(keyword).boost(2.0f) } }
                )
                .should(
                    Query.of { q -> q.match { m -> m.field("description").query(keyword) } }
                )
                .should(
                    Query.of { q ->
                        q.match { m ->
                            m.field("brandName").query(keyword).boost(1.5f)
                        }
                    }
                )
                .should(
                    Query.of { q -> q.match { m -> m.field("notes.text").query(keyword) } }
                )
                .should(
                    Query.of { q -> q.match { m -> m.field("accords.text").query(keyword) } }
                )
                .minimumShouldMatch("1")
                .build()

            boolQuery.must(Query.of { q -> q.bool(keywordQuery) })
        }

        // 브랜드 필터
        criteria.brandName?.let { brand ->
            boolQuery.filter(
                Query.of { q -> q.term { t -> t.field("brandName.keyword").value(brand) } }
            )
        }

        // 노트 필터
        criteria.note?.let { note ->
            boolQuery.filter(
                Query.of { q -> q.term { t -> t.field("notes.keyword").value(note) } }
            )
        }

        // 노트 타입별 필터
        if (criteria.noteType != null && criteria.note != null) {
            val nestedQuery = Query.of { q ->
                q.nested { n ->
                    n.path("notesByType")
                        .query { nq ->
                            nq.bool { b ->
                                b.must(
                                    Query.of { tq ->
                                        tq.term { t ->
                                            t.field("notesByType.type")
                                                .value(criteria.noteType.name)
                                        }
                                    }
                                )
                                b.must(
                                    Query.of { tq ->
                                        tq.term { t ->
                                            t.field("notesByType.notes.keyword")
                                                .value(criteria.note)
                                        }
                                    }
                                )
                            }
                        }
                        .scoreMode(ChildScoreMode.None)
                }
            }
            boolQuery.filter(nestedQuery)
        }

        // 어코드 필터
        criteria.accord?.let { accord ->
            boolQuery.filter(
                Query.of { q -> q.term { t -> t.field("accords.keyword").value(accord) } }
            )
        }

        // 출시 연도 범위 필터
        if (criteria.fromYear != null || criteria.toYear != null) {
            val rangeQuery = Query.of { q ->
                q.range { r ->
                    r.number { n ->
                        n.field("releaseYear")
                        if (criteria.fromYear != null) n.gte(criteria.fromYear.toDouble())
                        if (criteria.toYear != null) n.lte(criteria.toYear.toDouble())
                        n
                    }
                }
            }
            boolQuery.filter(rangeQuery)
        }
        // 평점 범위 필터
        if (criteria.minRating != null || criteria.maxRating != null) {
            val rangeQuery = Query.of { q ->
                q.range { r ->
                    r.number { n ->
                        n.field("averageRating")
                        if (criteria.minRating != null) n.gte(criteria.minRating)
                        if (criteria.maxRating != null) n.lte(criteria.maxRating)
                        n
                    }
                }
            }
            boolQuery.filter(rangeQuery)
        }

        // 쿼리 생성
        val nativeQuery = NativeQuery.builder()
            .withQuery(Query.of { q -> q.bool(boolQuery.build()) })
            .withPageable(criteria.pageable)

        // 정렬 조건 추가
        when (criteria.sortBy) {
            "newest" -> nativeQuery.withSort { s ->
                s.field { f ->
                    f.field("releaseYear").order(SortOrder.Desc)
                }
            }

            "rating" -> nativeQuery.withSort { s ->
                s.field { f ->
                    f.field("averageRating").order(SortOrder.Desc)
                }
            }

            "popularity" -> nativeQuery.withSort { s ->
                s.field { f ->
                    f.field("reviewCount").order(SortOrder.Desc)
                }
            }

            else -> nativeQuery.withSort { s -> s.score(ScoreSort.of { ss -> ss.order(SortOrder.Desc) }) }
        }

        // 검색 실행
        val searchHits = elasticsearchOperations.search(
            nativeQuery.build(),
            PerfumeDocument::class.java,
            IndexCoordinates.of("perfumes")
        )

        // 결과 변환
        return searchHits.map { hit ->
            PerfumeSearchResult.fromDocument(hit.content)
        }.toList()
    }

    // 유사한 향수 찾기
    fun findSimilarPerfumes(perfumeId: String, limit: Int): List<PerfumeSearchResult> {
        // 향수 문서 가져오기
        val perfume = elasticsearchOperations.get(perfumeId, PerfumeDocument::class.java)
            ?: return emptyList()

        // 유사성 쿼리 구성
        val boolQuery = BoolQuery.Builder()

        // 동일한 향수 제외
        boolQuery.mustNot(
            Query.of { q -> q.ids { i -> i.values(perfumeId) } }
        )

        // 노트 유사성 (가중치 높음)
        val notesQuery = BoolQuery.Builder()
        perfume.notes.forEach { note ->
            notesQuery.should(
                Query.of { q ->
                    q.term { t ->
                        t.field("notes.keyword").value(note).boost(2.0f)
                    }
                }
            )
        }
        boolQuery.should(Query.of { q -> q.bool(notesQuery.build()) })

        // 어코드 유사성
        val accordsQuery = BoolQuery.Builder()
        perfume.accords.forEach { accord ->
            accordsQuery.should(
                Query.of { q ->
                    q.term { t ->
                        t.field("accords.keyword").value(accord).boost(1.5f)
                    }
                }
            )
        }
        boolQuery.should(Query.of { q -> q.bool(accordsQuery.build()) })

        // 동일 브랜드 (약간의 가중치)
        boolQuery.should(
            Query.of { q ->
                q.term { t ->
                    t.field("brandName.keyword").value(perfume.brandName).boost(0.8f)
                }
            }
        )

        // 농도 유사성
        perfume.concentration?.let { concentration ->
            boolQuery.should(
                Query.of { q ->
                    q.term { t ->
                        t.field("concentration.keyword").value(concentration).boost(0.5f)
                    }
                }
            )
        }

        // 최소 매치 설정 (최소한 하나의 유사성은 있어야 함)
        boolQuery.minimumShouldMatch("1")

        // 쿼리 실행
        val nativeQuery = NativeQuery.builder()
            .withQuery(Query.of { q -> q.bool(boolQuery.build()) })
            .withPageable(PageRequest.of(0, limit))
            .build()

        val searchHits = elasticsearchOperations.search(
            nativeQuery,
            PerfumeDocument::class.java,
            IndexCoordinates.of("perfumes")
        )

        return searchHits.map { hit ->
            PerfumeSearchResult.fromDocument(hit.content)
        }.toList()
    }

    // 사용자 선호도 기반 추천
    fun findRecommendedPerfumes(
        memberPreferences: MemberPreferenceDto,
        limit: Int
    ): List<PerfumeSearchResult> {
        val boolQuery = BoolQuery.Builder()

        // 사용자가 이미 리뷰한 향수 제외
        if (memberPreferences.reviewedPerfumeIds.isNotEmpty()) {
            boolQuery.mustNot(
                Query.of { q ->
                    q.ids { i ->
                        i.values(memberPreference.reviewedPerfumeIds.map { it.toString() })
                    }
                }
            )
        }

        // 선호 노트 기반 추천
        if (memberPreferences.preferredNotes.isNotEmpty()) {
            val notesQuery = BoolQuery.Builder()
            memberPreferences.preferredNotes.forEach { note ->
                notesQuery.should(
                    Query.of { q ->
                        q.term { t ->
                            t.field("notes.keyword").value(note).boost(2.0f)
                        }
                    }
                )
            }
            boolQuery.should(Query.of { q -> q.bool(notesQuery.build()) })
        }

        // 선호 어코드 기반 추천
        if (memberPreferences.preferredAccords.isNotEmpty()) {
            val accordsQuery = BoolQuery.Builder()
            memberPreferences.preferredAccords.forEach { accord ->
                accordsQuery.should(
                    Query.of { q ->
                        q.term { t ->
                            t.field("accords.keyword").value(accord).boost(1.5f)
                        }
                    }
                )
            }
            boolQuery.should(Query.of { q -> q.bool(accordsQuery.build()) })
        }

        // 선호 브랜드 기반 추천
        if (memberPreferences.preferredBrands.isNotEmpty()) {
            val brandsQuery = BoolQuery.Builder()
            memberPreferences.preferredBrands.forEach { brand ->
                brandsQuery.should(
                    Query.of { q ->
                        q.term { t ->
                            t.field("brandName.keyword").value(brand).boost(1.0f)
                        }
                    }
                )
            }
            boolQuery.should(Query.of { q -> q.bool(brandsQuery.build()) })
        }

        // 최소 하나 이상 일치해야 함
        if (memberPreferences.preferredNotes.isNotEmpty() ||
            memberPreferences.preferredAccords.isNotEmpty() ||
            memberPreferences.preferredBrands.isNotEmpty()
        ) {
            boolQuery.minimumShouldMatch("1")
        }

        // 쿼리 실행
        val nativeQuery = NativeQuery.builder()
            .withQuery(Query.of { q -> q.bool(boolQuery.build()) })
            .withPageable(PageRequest.of(0, limit))
            .build()

        val searchHits = elasticsearchOperations.search(
            nativeQuery,
            PerfumeDocument::class.java,
            IndexCoordinates.of("perfumes")
        )

        return searchHits.map { hit ->
            PerfumeSearchResult.fromDocument(hit.content)
        }.toList()
    }
}