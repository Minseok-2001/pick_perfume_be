package ym_cosmetic.pick_perfume_be.perfume.vo

enum class Season(val displayName: String) {
    WINTER("Winter"),
    SPRING("Spring"),
    SUMMER("Summer"),
    FALL("Fall");

    companion object {
        fun fromString(value: String?): Season? {
            return value?.let { input ->
                values().find { it.name.equals(input, ignoreCase = true) }
            }
        }
    }
}