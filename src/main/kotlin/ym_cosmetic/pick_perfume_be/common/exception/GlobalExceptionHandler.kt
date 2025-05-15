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

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    fun handleUnauthorizedException(ex: UnauthorizedException): ApiResponse<Nothing> {
        return ApiResponse.error(
            message = ex.message ?: "Unauthorized",
            code = "UNAUTHORIZED",
            status = HttpStatus.UNAUTHORIZED.value()
        )
    }

    @ExceptionHandler(InvalidRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun handleInvalidRequestException(ex: InvalidRequestException): ApiResponse<Nothing> {
        return ApiResponse.error(
            message = ex.message ?: "Invalid request",
            code = "INVALID_REQUEST",
            status = HttpStatus.BAD_REQUEST.value()
        )
    }

    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    fun handleForbiddenException(ex: ForbiddenException): ApiResponse<Nothing> {
        return ApiResponse.error(
            message = ex.message ?: "Forbidden",
            code = "FORBIDDEN",
            status = HttpStatus.FORBIDDEN.value()
        )
    }


}