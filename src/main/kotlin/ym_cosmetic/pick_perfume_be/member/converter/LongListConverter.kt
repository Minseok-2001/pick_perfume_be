package ym_cosmetic.pick_perfume_be.member.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class LongListConverter : AttributeConverter<List<Long>, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<Long>?): String {
        return objectMapper.writeValueAsString(attribute ?: emptyList<Long>())
    }

    override fun convertToEntityAttribute(dbData: String?): List<Long> {
        if (dbData.isNullOrBlank()) return emptyList()
        return objectMapper.readValue(dbData, object : TypeReference<List<Long>>() {})
    }
}