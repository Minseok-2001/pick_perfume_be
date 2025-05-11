package ym_cosmetic.pick_perfume_be.perfume.vo

enum class Concentration(
    val displayName: String,
    val oilPercentage: Pair<Double, Double> // 오일 농도 범위(%)
) {
    PARFUM("Parfum", 20.0 to 30.0),
    EAU_DE_PARFUM("Eau de Parfum", 15.0 to 20.0),
    EAU_DE_TOILETTE("Eau de Toilette", 5.0 to 15.0),
    EAU_DE_COLOGNE("Eau de Cologne", 2.0 to 4.0),
    BODY_SPRAY("Body Spray", 0.5 to 1.0);

    fun getAverageLastingHours(): Int {
        return when (this) {
            PARFUM -> 12
            EAU_DE_PARFUM -> 8
            EAU_DE_TOILETTE -> 5
            EAU_DE_COLOGNE -> 2
            BODY_SPRAY -> 1
        }
    }

    companion object {
        fun fromDisplayName(name: String): Concentration? {
            return values().find { it.displayName.equals(name, ignoreCase = true) }
        }

        fun fromAbbreviation(abbr: String): Concentration? {
            return when (abbr.uppercase()) {
                "PARFUM", "PERFUME", "EXTRAIT" -> PARFUM
                "EDP", "EAU DE PARFUM" -> EAU_DE_PARFUM
                "EDT", "EAU DE TOILETTE" -> EAU_DE_TOILETTE
                "EDC", "EAU DE COLOGNE" -> EAU_DE_COLOGNE
                "BODY SPRAY", "BODY MIST" -> BODY_SPRAY
                else -> null
            }
        }
    }
}