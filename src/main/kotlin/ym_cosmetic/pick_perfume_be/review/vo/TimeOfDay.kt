package ym_cosmetic.pick_perfume_be.review.vo;

enum class TimeOfDay(val displayName: String) {
    DAY("Day"),
    NIGHT("Night");
    
    companion object {
        fun fromString(value: String?): TimeOfDay? {
            return value?.let { input ->
                values().find { it.name.equals(input, ignoreCase = true) }
            }
        }
    }
}