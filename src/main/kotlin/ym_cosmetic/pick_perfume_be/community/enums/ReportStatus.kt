package ym_cosmetic.pick_perfume_be.community.enums

enum class ReportStatus {
    REPORTED,    // 신고 접수됨
    PROCESSING,  // 처리 중
    ACCEPTED,    // 신고 승인 (처리 완료)
    REJECTED     // 신고 거부 (처리 완료)
} 