package ym_cosmetic.pick_perfume_be.search.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*
import ym_cosmetic.pick_perfume_be.vote.vo.VoteCategory
import java.time.LocalDate
import java.time.LocalDateTime

@Document(indexName = "perfumes")
class PerfumeDocument(
    @Id
    val id: String,

    @Field(type = FieldType.Text, analyzer = "standard")
    val name: String,

    @Field(type = FieldType.Text, analyzer = "standard")
    val description: String?,

    @Field(type = FieldType.Keyword)
    val brandName: String,

    @Field(type = FieldType.Integer)
    val releaseYear: Int?,

    @Field(type = FieldType.Keyword)
    val concentration: String?,

    @Field(type = FieldType.Keyword)
    @MultiField(
        mainField = Field(type = FieldType.Keyword),
        otherFields = [
            InnerField(suffix = "text", type = FieldType.Text, analyzer = "standard")
        ]
    )
    val notes: List<String>,

    @Field(type = FieldType.Nested)
    val notesByType: List<NotesByType>,

    @Field(type = FieldType.Keyword)
    @MultiField(
        mainField = Field(type = FieldType.Keyword),
        otherFields = [
            InnerField(suffix = "text", type = FieldType.Text, analyzer = "standard")
        ]
    )
    val accords: List<String>,

    @Field(type = FieldType.Nested)
    val designers: List<DesignerInfo>,

    @Field(type = FieldType.Float)
    val averageRating: Float,

    @Field(type = FieldType.Integer)
    val reviewCount: Int,

    @Field(type = FieldType.Nested)
    val voteResults: Map<VoteCategory, Map<String, Int>>,

    @Field(type = FieldType.Date, format = [DateFormat.date])
    val releaseDate: LocalDate?,

    @Field(type = FieldType.Keyword)
    val tags: List<String> = emptyList(),

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    val createdAt: LocalDateTime,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    val updatedAt: LocalDateTime
)

data class NotesByType(
    @Field(type = FieldType.Keyword)
    val type: String, // "TOP", "MIDDLE", "BASE"

    @Field(type = FieldType.Keyword)
    @MultiField(
        mainField = Field(type = FieldType.Keyword),
        otherFields = [
            InnerField(suffix = "text", type = FieldType.Text, analyzer = "standard")
        ]
    )
    val notes: List<String>
)

data class DesignerInfo(
    @Field(type = FieldType.Keyword)
    val id: String,

    @Field(type = FieldType.Text, analyzer = "standard")
    val name: String,

    @Field(type = FieldType.Keyword)
    val role: String
)