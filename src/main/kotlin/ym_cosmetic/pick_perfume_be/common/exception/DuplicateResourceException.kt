package ym_cosmetic.pick_perfume_be.common.exception

/**
 * 이미 존재하는(중복된) 리소스에 대한 예외
 */
class DuplicateResourceException(message: String) : RuntimeException(message) 