package ym_cosmetic.pick_perfume_be.designer.dto.response

import ym_cosmetic.pick_perfume_be.common.dto.response.CountryResponse
import ym_cosmetic.pick_perfume_be.designer.entity.Designer
import java.time.LocalDate

data class DesignerResponse(
    val id: Long,
    val name: String,
    val biography: String?,
    val birthDate: LocalDate?,
    val country: CountryResponse?,
    val photoUrl: String?,
    val website: String?,
    val socialMediaHandle: String?,
) {
    companion object {
        fun from(designer: Designer): DesignerResponse {
            return DesignerResponse(
                id = designer.id!!,
                name = designer.name,
                biography = designer.biography,
                birthDate = designer.birthDate,
                country = designer.country?.let {
                    CountryResponse(it.code, it.name, it.getContinent())
                },
                photoUrl = designer.photo?.url,
                website = designer.website,
                socialMediaHandle = designer.socialMediaHandle,
            )
        }
    }
}

