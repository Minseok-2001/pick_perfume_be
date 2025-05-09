package ym_cosmetic.pick_perfume_be.common.dto.response

import org.slf4j.MDC
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.common.exception.InvalidRequestException
import ym_cosmetic.pick_perfume_be.common.exception.UnauthorizedException
import java.time.LocalDateTime

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val requestId: String? = MDC.get("requestId")
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data
            )
        }

        fun <T> error(message: String, code: String = "ERROR", status: Int = 400): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ApiError(message, code, status)
            )
        }

        fun <T> fromException(ex: Exception): ApiResponse<T> {
            return when (ex) {
                is EntityNotFoundException -> error(
                    message = ex.message ?: "Entity not found",
                    code = "ENTITY_NOT_FOUND",
                    status = 404
                )

                is InvalidRequestException -> error(
                    message = ex.message ?: "Invalid request",
                    code = "INVALID_REQUEST",
                    status = 400
                )

                is UnauthorizedException -> error(
                    message = ex.message ?: "Unauthorized",
                    code = "UNAUTHORIZED",
                    status = 401
                )

                else -> error(
                    message = "An unexpected error occurred: ${ex.message}",
                    code = "INTERNAL_SERVER_ERROR",
                    status = 500
                )
            }
        }
    }

    data class ApiError(
        val message: String,
        val code: String,
        val status: Int,
        val details: Map<String, Any> = emptyMap() // 상세 에러 정보 추가
    )
}