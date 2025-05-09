package ym_cosmetic.pick_perfume_be.security

import ym_cosmetic.pick_perfume_be.member.enums.UserRole

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireRole(vararg val value: UserRole = [])