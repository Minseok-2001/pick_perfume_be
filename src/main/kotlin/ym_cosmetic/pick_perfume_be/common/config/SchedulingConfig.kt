package ym_cosmetic.pick_perfume_be.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import ym_cosmetic.pick_perfume_be.recommendation.service.MemberPreferenceService

@Configuration
@EnableScheduling
class SchedulingConfig(
    private val memberPreferenceService: MemberPreferenceService
) {
    // 매주 월요일 새벽 3시에 모든 회원의 선호도 업데이트
    @Scheduled(cron = "0 0 3 ? * MON")
    fun updateAllMemberPreferences() {
        memberPreferenceService.updateAllMemberPreferences()
    }
}