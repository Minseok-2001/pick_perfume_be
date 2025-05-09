package ym_cosmetic.pick_perfume_be.search.document

import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

data class DesignerInfo(
    @Field(type = FieldType.Keyword)
    val id: String,

    @Field(type = FieldType.Text, analyzer = "standard")
    val name: String,

    @Field(type = FieldType.Keyword)
    val role: String
)