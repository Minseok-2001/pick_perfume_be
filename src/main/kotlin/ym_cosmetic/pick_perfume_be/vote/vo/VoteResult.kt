package ym_cosmetic.pick_perfume_be.vote.vo

import ym_cosmetic.pick_perfume_be.vote.entity.Vote

data class VoteResult(
    val category: VoteCategory,
    val counts: Map<String, Int>,
    val totalVotes: Int,
    val topValue: String?
) {
    fun getPercentage(value: String): Int {
        if (totalVotes == 0) return 0
        return (counts[value] ?: 0) * 100 / totalVotes
    }

    fun getTopValueDisplay(): String? {
        return topValue?.let { category.getDisplayForValue(it) }
    }

    companion object {
        fun fromVotes(category: VoteCategory, votes: List<Vote>): VoteResult {
            val counts = votes.filter { it.category == category }
                .groupBy { it.value }
                .mapValues { it.value.size }

            val totalVotes = counts.values.sum()

            val topValue = if (counts.isEmpty()) null
            else counts.maxByOrNull { it.value }?.key

            return VoteResult(
                category = category,
                counts = counts,
                totalVotes = totalVotes,
                topValue = topValue
            )
        }
    }
}