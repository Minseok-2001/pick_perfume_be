package ym_cosmetic.pick_perfume_be.search.document

import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.InnerField
import org.springframework.data.elasticsearch.annotations.MultiField

data class AccordDocument(
    @Field(type = FieldType.Long)
    val id: Long,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "korean"),
        otherFields = [
            InnerField(suffix = "keyword", type = FieldType.Keyword),
            InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer")
        ]
    )
    val name: String
)