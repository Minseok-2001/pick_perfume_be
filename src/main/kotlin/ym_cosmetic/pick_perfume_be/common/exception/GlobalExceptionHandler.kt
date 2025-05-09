package ym_cosmetic.pick_perfume_be.common.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    fun handleEntityNotFoundException(ex: EntityNotFoundException): ApiResponse<Nothing> {
        return ApiResponse.error(
            message = ex.message ?: "Entity not found",
            code = "ENTITY_NOT_FOUND",
            status = HttpStatus.NOT_FOUND.value()
        )
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun handleGenericException(ex: Exception): ApiResponse<Nothing> {
        return ApiResponse.error(
            message = "An unexpected error occurred: ${ex.message}",
            code = "INTERNAL_SERVER_ERROR",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
    }
}