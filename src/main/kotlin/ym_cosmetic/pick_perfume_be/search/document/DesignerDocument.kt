package ym_cosmetic.pick_perfume_be.search.document

import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.InnerField
import org.springframework.data.elasticsearch.annotations.MultiField

data class DesignerDocument(
    @Field(type = FieldType.Long)
    val id: Long,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "korean"),
        otherFields = [
            InnerField(suffix = "keyword", type = FieldType.Keyword)
        ]
    )
    val name: String,

    @Field(type = FieldType.Keyword)
    val role: String
)