package ym_cosmetic.pick_perfume_be.common.exception

class UnauthorizedException : RuntimeException {
    constructor(message: String?) : super(message)

    constructor() : super("로그인이 필요합니다.")
}
