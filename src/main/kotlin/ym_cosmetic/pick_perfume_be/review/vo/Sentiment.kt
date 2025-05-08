package ym_cosmetic.pick_perfume_be.review.vo


enum class Sentiment(val displayName: String) {
    LOVE("Love"),
    LIKE("Like"),
    OK("OK"),
    DISLIKE("Dislike"),
    HATE("Hate");

    companion object {
        fun fromString(value: String?): Sentiment? {
            return value?.let { input ->
                values().find { it.name.equals(input, ignoreCase = true) }
            }
        }
    }
}
