package ym_cosmetic.pick_perfume_be.common.dto.response

import java.time.LocalDateTime

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
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
    }

    data class ApiError(
        val message: String,
        val code: String,
        val status: Int
    )
}