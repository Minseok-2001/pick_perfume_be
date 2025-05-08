package ym_cosmetic.pick_perfume_be.perfume.vo

enum class Concentration(
    val displayName: String,
    val oilPercentage: Pair<Double, Double> // 오일 농도 범위(%)
) {
    PARFUM("Parfum", 20.0 to 30.0),
    EDP("Eau de Parfum", 15.0 to 20.0),
    EDT("Eau de Toilette", 5.0 to 15.0),
    EDC("Eau de Cologne", 2.0 to 4.0),
    BODY_SPRAY("Body Spray", 0.5 to 1.0);

    fun getAverageLastingHours(): Int {
        return when (this) {
            PARFUM -> 12
            EDP -> 8
            EDT -> 5
            EDC -> 2
            BODY_SPRAY -> 1
        }
    }
}