package ym_cosmetic.pick_perfume_be.batch.dto

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvDate

data class PerfumeImportDto(
    @CsvBindByName(column = "id")
    var id: Long? = null,

    @CsvBindByName(column = "name", required = true)
    var name: String = "",

    @CsvBindByName(column = "brand_name", required = true)
    var brandName: String = "",

    @CsvBindByName(column = "description")
    var description: String? = null,

    @CsvBindByName(column = "release_year")
    var releaseYear: Int? = null,

    @CsvBindByName(column = "concentration")
    var concentration: String? = null,

    @CsvBindByName(column = "image_url")
    var imageUrl: String? = null,

    @CsvBindByName(column = "top_notes")
    var topNotes: String? = null,

    @CsvBindByName(column = "middle_notes")
    var middleNotes: String? = null,

    @CsvBindByName(column = "base_notes")
    var baseNotes: String? = null,

    @CsvBindByName(column = "accords")
    var accords: String? = null,

    @CsvBindByName(column = "perfumer")
    var perfumer: String? = null,

    @CsvBindByName(column = "gender")
    var gender: String? = null,

    @CsvBindByName(column = "seasons")
    var seasons: String? = null
) 