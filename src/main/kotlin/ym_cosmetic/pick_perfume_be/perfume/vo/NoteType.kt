package ym_cosmetic.pick_perfume_be.perfume.vo

enum class NoteType(val displayName: String, val description: String) {
    TOP("Top Note", "The initial impression that quickly fades"),
    MIDDLE("Middle Note", "The heart of the fragrance that emerges after top notes fade"),
    BASE("Base Note", "The long-lasting foundation of the fragrance");

    fun getTypicalLastingTime(): String {
        return when (this) {
            TOP -> "15 minutes to 2 hours"
            MIDDLE -> "2 to 4 hours"
            BASE -> "4 hours to days"
        }
    }
}