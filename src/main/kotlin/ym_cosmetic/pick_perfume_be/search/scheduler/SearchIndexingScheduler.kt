package ym_cosmetic.pick_perfume_be.search.scheduler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ym_cosmetic.pick_perfume_be.recommendation.service.MemberPreferenceAnalysisService
import ym_cosmetic.pick_perfume_be.search.service.PerfumeIndexingService

@Component
class SearchIndexingScheduler(
    private val perfumeIndexingService: PerfumeIndexingService,
    private val memberPreferenceAnalysisService: MemberPreferenceAnalysisService
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // 매일 새벽 2시에 전체 향수 데이터 인덱싱
    @Scheduled(cron = "0 0 2 * * ?")
    fun reindexAllPerfumes() {
        coroutineScope.launch {
            perfumeIndexingService.indexAllPerfumes()
        }
    }

    // 매주 월요일 새벽 3시에 모든 사용자 선호도 분석
    @Scheduled(cron = "0 0 3 ? * MON")
    fun analyzeAllUserPreferences() {
        coroutineScope.launch {
            memberPreferenceAnalysisService.analyzeAllUserPreferences()
        }
    }
}