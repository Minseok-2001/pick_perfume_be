package ym_cosmetic.pick_perfume_be.common.dto.response

import org.slf4j.MDC
import java.time.LocalDateTime

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
    val message: String? = null,
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

        fun <T> success(message: String, data: T): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message
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
        val status: Int,
        val details: Map<String, Any> = emptyMap() // 상세 에러 정보 추가
    )
}