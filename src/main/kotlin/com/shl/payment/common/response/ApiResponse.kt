package com.shl.payment.common.response

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null,
) {
    companion object {
        fun <T> ok(data: T) = ApiResponse(success = true, data = data)
        fun ok() = ApiResponse<Unit>(success = true)
        fun fail(error: ErrorResponse) = ApiResponse<Unit>(success = false, error = error)
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
)
