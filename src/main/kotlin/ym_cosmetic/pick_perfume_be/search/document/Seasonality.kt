package ym_cosmetic.pick_perfume_be.search.document

import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

data class Seasonality(
    @Field(type = FieldType.Float)
    val spring: Float = 0f,

    @Field(type = FieldType.Float)
    val summer: Float = 0f,

    @Field(type = FieldType.Float)
    val fall: Float = 0f,

    @Field(type = FieldType.Float)
    val winter: Float = 0f
)
