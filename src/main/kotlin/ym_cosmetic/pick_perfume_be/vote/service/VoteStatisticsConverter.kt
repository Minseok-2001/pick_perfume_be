package ym_cosmetic.pick_perfume_be.vote.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class VoteStatisticsConverter : AttributeConverter<Map<String, Map<String, Int>>, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: Map<String, Map<String, Int>>): String {
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): Map<String, Map<String, Int>> {
        return objectMapper.readValue(
            dbData,
            object : TypeReference<Map<String, Map<String, Int>>>() {})
    }
}