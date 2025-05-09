package ym_cosmetic.pick_perfume_be.vote.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ym_cosmetic.pick_perfume_be.vote.vo.VoteCategory


data class VoteCreateRequest(
    /**
     * 투표할 향수 ID
     */
    @field:NotNull(message = "향수 ID는 필수입니다")
    val perfumeId: Long,

    /**
     * 투표 카테고리 (예: LONGEVITY, SILLAGE, GENDER, PRICE_VALUE)
     */
    @field:NotNull(message = "투표 카테고리는 필수입니다")
    val category: VoteCategory,

    /**
     * 투표 값 (카테고리에 따라 허용되는 값이 다름)
     * LONGEVITY: very_weak, weak, moderate, long_lasting, eternal
     * SILLAGE: intimate, moderate, strong, enormous
     * GENDER: female, more_female, unisex, more_male, male
     * PRICE_VALUE: way_overpriced, overpriced, ok, good_value, great_value
     */
    @field:NotBlank(message = "투표 값은 필수입니다")
    val value: String
) {
    fun isValidVote(): Boolean {
        return category.isValidValue(value)
    }
}