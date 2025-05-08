package ym_cosmetic.pick_perfume_be.vote.entity.vo;



enum class VoteCategory(val displayName: String) {
    LONGEVITY("Longevity"),
    SILLAGE("Sillage"),
    GENDER("Gender"),
    PRICE_VALUE("Price Value");
    
    fun getAllowedValues(): List<String> {
        return when(this) {
            LONGEVITY -> listOf("very_weak", "weak", "moderate", "long_lasting", "eternal")
            SILLAGE -> listOf("intimate", "moderate", "strong", "enormous") 
            GENDER -> listOf("female", "more_female", "unisex", "more_male", "male")
            PRICE_VALUE -> listOf("way_overpriced", "overpriced", "ok", "good_value", "great_value")
        }
    }
    
    fun isValidValue(value: String): Boolean {
        return getAllowedValues().contains(value.lowercase())
    }
}