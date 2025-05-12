package ym_cosmetic.pick_perfume_be.brand.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.brand.vo.Country
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl

@Entity
@Table(name = "brand")
class Brand(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column
    var content: String? = null,

    @Column
    var foundedYear: Int? = null,

    @Column
    var website: String? = null,

    @Embedded
    var logo: ImageUrl? = null,

    @Embedded
    var country: Country? = null,

    @Column
    var designer: String? = null,

    @Column(nullable = false)
    var isLuxury: Boolean = false,

    @Column(nullable = false)
    var isNiche: Boolean = false,

    @Column(nullable = false)
    var isPopular: Boolean = false,

    ) : BaseTimeEntity() {

    companion object {
        fun create(
            name: String,
            content: String? = null,
            foundedYear: Int? = null,
            website: String? = null,
            logo: ImageUrl? = null,
            country: Country? = null,
            designer: String? = null,
            isLuxury: Boolean = false,
            isNiche: Boolean = false,
            isPopular: Boolean = false
        ): Brand {
            return Brand(
                name = name,
                content = content,
                foundedYear = foundedYear,
                website = website,
                logo = logo,
                country = country,
                designer = designer,
                isLuxury = isLuxury,
                isNiche = isNiche,
                isPopular = isPopular
            )
        }
    }

    fun updateDetails(
        name: String,
        content: String?,
        foundedYear: Int?,
        website: String?,
        designer: String?,
        isLuxury: Boolean,
        isNiche: Boolean,
        isPopular: Boolean
    ) {
        this.name = name
        this.content = content
        this.foundedYear = foundedYear
        this.website = website
        this.designer = designer
        this.isLuxury = isLuxury
        this.isNiche = isNiche
        this.isPopular = isPopular
    }

    fun updateLogo(logo: ImageUrl?) {
        this.logo = logo
    }

    fun updateCountry(countryCode: String) {
        if (Country.isValidCountryCode(countryCode)) {
            this.country = Country.of(countryCode)
        }
    }
}