package ym_cosmetic.pick_perfume_be.review.vo

import jakarta.persistence.Embeddable

@Embeddable
data class Rating private constructor(val value: Int) {
    companion object {
        fun of(value: Int): Rating {
            require(value in 1..5) { "Rating must be between 1 and 5" }
            return Rating(value)
        }
    }

    fun getcontent(): String {
        return when (value) {
            1 -> "Poor"
            2 -> "Below Average"
            3 -> "Average"
            4 -> "Good"
            5 -> "Excellent"
            else -> "Invalid Rating"
        }
    }
}