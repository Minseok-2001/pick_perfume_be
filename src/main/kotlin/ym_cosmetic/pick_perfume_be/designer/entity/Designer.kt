package ym_cosmetic.pick_perfume_be.designer.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.brand.vo.Country
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import java.time.LocalDate

@Entity
@Table(name = "designer")
class Designer(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(length = 5000)
    var biography: String? = null,

    @Column
    var birthDate: LocalDate? = null,

    @Embedded
    var country: Country? = null,

    @Embedded
    var photo: ImageUrl? = null,

    @Column
    var website: String? = null,

    @Column
    var socialMediaHandle: String? = null,

    ) : BaseTimeEntity() {

    companion object {
        fun create(
            name: String,
            biography: String? = null,
            birthDate: LocalDate? = null,
            country: Country? = null,
            photo: ImageUrl? = null,
            website: String? = null,
            socialMediaHandle: String? = null
        ): Designer {
            return Designer(
                name = name,
                biography = biography,
                birthDate = birthDate,
                country = country,
                photo = photo,
                website = website,
                socialMediaHandle = socialMediaHandle
            )
        }
    }

    fun updateDetails(
        name: String,
        biography: String?,
        birthDate: LocalDate?,
        website: String?,
        socialMediaHandle: String?,
    ) {
        this.name = name
        this.biography = biography
        this.birthDate = birthDate
        this.website = website
        this.socialMediaHandle = socialMediaHandle
    }

    fun updatePhoto(photo: ImageUrl?) {
        this.photo = photo
    }

    fun updateCountry(countryCode: String) {
        if (Country.isValidCountryCode(countryCode)) {
            this.country = Country.of(countryCode)
        }
    }
}