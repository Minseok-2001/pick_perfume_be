package ym_cosmetic.pick_perfume_be.common.exception

class EntityNotFoundException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(entityName: String, id: Long) : super("$entityName ID $id 에 해당하는 데이터를 찾을 수 없습니다.")
}
