package ym_cosmetic.pick_perfume_be.brand.vo

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Country(
    @Column(name = "country_code", length = 2)
    val code: String,

    @Column(name = "country_name")
    val name: String
) {
    companion object {
        fun of(code: String): Country {
            val countryName = when (code.uppercase()) {
                "FR" -> "France"
                "IT" -> "Italy"
                "US" -> "United States"
                "UK" -> "United Kingdom"
                "JP" -> "Japan"
                "KR" -> "South Korea"
                else -> code // 또는 기본값이나 예외 처리
            }
            return Country(code.uppercase(), countryName)
        }

        // ISO 국가 코드가 유효한지 검증하는 메서드
        fun isValidCountryCode(code: String): Boolean {
            return code.length == 2 && code.all { it.isLetter() }
        }
    }

    init {
        require(code.length == 2 && code.all { it.isLetter() }) {
            "Country code must be a 2-letter ISO code"
        }
    }

    fun getContinent(): String {
        return when (code.uppercase()) {
            "FR", "IT", "UK", "DE", "ES" -> "Europe"
            "US", "CA", "MX" -> "North America"
            "JP", "KR", "CN", "IN" -> "Asia"
            // 다른 대륙 추가
            else -> "Unknown"
        }
    }

    // 국기 이모지 반환 (선택적)
    fun getFlagEmoji(): String {
        // 국가 코드를 국기 이모지로 변환
        val base = 127397 // 유니코드 리전 인디케이터 심볼 베이스 코드
        return code.uppercase().map { it.code + base }
            .joinToString("") { String(Character.toChars(it)) }
    }
}