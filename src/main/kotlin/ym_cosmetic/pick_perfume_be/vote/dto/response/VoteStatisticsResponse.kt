package ym_cosmetic.pick_perfume_be.vote.dto.response

import ym_cosmetic.pick_perfume_be.vote.vo.VoteCategory
import ym_cosmetic.pick_perfume_be.vote.vo.VoteResult

data class VoteStatisticsResponse(
    val categories: List<VoteCategoryStatistics>
)

data class VoteCategoryStatistics(
    val category: String,
    val displayName: String,
    val description: String,
    val totalVotes: Int,
    val values: List<VoteValueStatistics>,
    val topValue: String?,
    val topValueDisplay: String?
) {
    companion object {
        fun from(category: VoteCategory, result: VoteResult): VoteCategoryStatistics {
            return VoteCategoryStatistics(
                category = category.name,
                displayName = category.displayName,
                description = category.description,
                totalVotes = result.totalVotes,
                values = category.getSortedValues().map { value ->
                    VoteValueStatistics(
                        value = value,
                        display = category.getDisplayForValue(value),
                        count = result.counts[value] ?: 0,
                        percentage = result.getPercentage(value)
                    )
                },
                topValue = result.topValue,
                topValueDisplay = result.getTopValueDisplay()
            )
        }
    }
}

