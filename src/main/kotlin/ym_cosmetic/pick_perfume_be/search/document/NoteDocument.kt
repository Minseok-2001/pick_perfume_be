package ym_cosmetic.pick_perfume_be.search.document

import org.springframework.data.elasticsearch.annotations.*

@Document(indexName = "perfumes")
data class NoteDocument(
    @Field(type = FieldType.Long)
    val id: Long,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "korean"),
        otherFields = [
            InnerField(suffix = "keyword", type = FieldType.Keyword),
            InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer")
        ]
    )
    val name: String,

    @Field(type = FieldType.Keyword)
    val type: String
)
