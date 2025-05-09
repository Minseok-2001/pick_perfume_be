package ym_cosmetic.pick_perfume_be.brand.service

import org.springframework.stereotype.Service
import ym_cosmetic.pick_perfume_be.brand.repository.BrandRepository

@Service
class BrandService(
    private val brandRepository: BrandRepository
) {
//    @Transactional
//    fun createBrand(request: BrandCreateRequest): BrandResponse {
//        val country = request.countryCode?.let {
//            try {
//                Country.of(it)
//            } catch (e: IllegalArgumentException) {
//                null // 유효하지 않은 국가 코드는 무시
//            }
//        }
//
//        val brand = Brand(
//            name = request.name,
//            description = request.description,
//            foundedYear = request.foundedYear,
//            website = request.website,
//            logo = request.logoUrl?.let { ImageUrl(it) },
//            country = country,
//            designer = request.designer,
//            isLuxury = request.isLuxury,
//            isNiche = request.isNiche,
//            isPopular = request.isPopular
//        )
//
//        val savedBrand = brandRepository.save(brand)
//        return BrandResponse.from(savedBrand)
//    }
//
//    @Transactional(readOnly = true)
//    fun getBrandsByCountry(countryCode: String): List<BrandResponse> {
//        val country = Country.of(countryCode)
//        return brandRepository.findByCountryCode(country.code)
//            .map { BrandResponse.from(it) }
//    }
}