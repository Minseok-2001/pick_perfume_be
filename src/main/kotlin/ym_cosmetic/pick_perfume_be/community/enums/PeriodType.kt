package ym_cosmetic.pick_perfume_be.community.enums

import java.time.LocalDateTime

enum class PeriodType(val period: (LocalDateTime) -> LocalDateTime) {
    DAILY({ now -> now.minusDays(1) }), // 일간
    WEEKLY({ now -> now.minusWeeks(1) }), // 주간
    MONTHLY({ now -> now.minusMonths(1) }) // 월간
} 