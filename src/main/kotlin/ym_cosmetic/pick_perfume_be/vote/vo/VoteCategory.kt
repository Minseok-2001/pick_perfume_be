package ym_cosmetic.pick_perfume_be.vote.vo

enum class VoteCategory(val displayName: String, val description: String) {
    LONGEVITY("Longevity", "How long the fragrance lasts"),
    SILLAGE("Sillage", "How far the fragrance projects"),
    GENDER("Gender", "How masculine or feminine the fragrance is perceived"),
    PRICE_VALUE("Price Value", "How good the value is for the price");

    fun getAllowedValues(): List<String> {
        return when (this) {
            LONGEVITY -> listOf("very_weak", "weak", "moderate", "long_lasting", "eternal")
            SILLAGE -> listOf("intimate", "moderate", "strong", "enormous")
            GENDER -> listOf("female", "more_female", "unisex", "more_male", "male")
            PRICE_VALUE -> listOf("way_overpriced", "overpriced", "ok", "good_value", "great_value")
        }
    }

    fun isValidValue(value: String): Boolean {
        return getAllowedValues().contains(value.toLowerCase())
    }

    // 값에 대한 표시 이름 지정 (UI에 친숙한 형태)
    fun getDisplayForValue(value: String): String {
        return when (this) {
            LONGEVITY -> when (value) {
                "very_weak" -> "Very Weak (< 2 hours)"
                "weak" -> "Weak (2-4 hours)"
                "moderate" -> "Moderate (4-6 hours)"
                "long_lasting" -> "Long Lasting (6-8 hours)"
                "eternal" -> "Eternal (> 8 hours)"
                else -> value
            }

            SILLAGE -> when (value) {
                "intimate" -> "Intimate (arm's length)"
                "moderate" -> "Moderate (within room)"
                "strong" -> "Strong (fills a room)"
                "enormous" -> "Enormous (can be smelled from distance)"
                else -> value
            }

            GENDER -> when (value) {
                "female" -> "Feminine"
                "more_female" -> "Leans Feminine"
                "unisex" -> "Unisex"
                "more_male" -> "Leans Masculine"
                "male" -> "Masculine"
                else -> value
            }

            PRICE_VALUE -> when (value) {
                "way_overpriced" -> "Way Overpriced"
                "overpriced" -> "Overpriced"
                "ok" -> "Acceptable"
                "good_value" -> "Good Value"
                "great_value" -> "Great Value"
                else -> value
            }
        }
    }

    fun getSortedValues(): List<String> {
        return when (this) {
            LONGEVITY -> listOf("very_weak", "weak", "moderate", "long_lasting", "eternal")
            SILLAGE -> listOf("intimate", "moderate", "strong", "enormous")
            GENDER -> listOf("female", "more_female", "unisex", "more_male", "male")
            PRICE_VALUE -> listOf("way_overpriced", "overpriced", "ok", "good_value", "great_value")
        }
    }
}