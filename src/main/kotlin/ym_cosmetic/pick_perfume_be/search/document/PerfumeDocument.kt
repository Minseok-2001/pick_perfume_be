package ym_cosmetic.pick_perfume_be.search.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*
import java.time.LocalDateTime

@Document(indexName = "perfumes")
@Setting(settingPath = "es-settings.json")
data class PerfumeDocument(
    @Id
    val id: Long,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "korean"),
        otherFields = [
            InnerField(suffix = "keyword", type = FieldType.Keyword),
            InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer")
        ]
    )
    val name: String,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "korean"),
        otherFields = [
            InnerField(suffix = "standard", type = FieldType.Text, analyzer = "standard")
        ]
    )
    val content: String?,

    @Field(type = FieldType.Integer)
    val releaseYear: Int?,

    @MultiField(
        mainField = Field(type = FieldType.Keyword),
        otherFields = [
            InnerField(suffix = "text", type = FieldType.Text, analyzer = "korean")
        ]
    )
    val brandName: String,

    @Field(type = FieldType.Long)
    val brandId: Long,

    @Field(type = FieldType.Keyword)
    val concentration: String?,

    @Field(type = FieldType.Keyword)
    val imageUrl: String?,

    @Field(type = FieldType.Nested, includeInParent = true)
    val notes: List<NoteDocument>,

    @Field(type = FieldType.Nested, includeInParent = true)
    val accords: List<AccordDocument>,

    @Field(type = FieldType.Nested)
    val designers: List<DesignerDocument>,

    @Field(type = FieldType.Double)
    val averageRating: Double,

    @Field(type = FieldType.Integer)
    val reviewCount: Int,

    @Field(type = FieldType.Boolean)
    val isApproved: Boolean,

    @Field(type = FieldType.Object)
    val seasonality: Seasonality? = null,

    @Field(type = FieldType.Keyword)
    val gender: String? = null,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    val createdAt: LocalDateTime,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    val updatedAt: LocalDateTime
)
