package ym_cosmetic.pick_perfume_be.search.document

import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.InnerField
import org.springframework.data.elasticsearch.annotations.MultiField

data class NotesByType(
    @Field(type = FieldType.Keyword)
    val type: String, // "TOP", "MIDDLE", "BASE"

    @MultiField(
        mainField = Field(type = FieldType.Keyword),
        otherFields = [
            InnerField(suffix = "text", type = FieldType.Text, analyzer = "standard")
        ]
    )
    val notes: List<String>
)