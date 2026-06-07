package com.shl.payment.common.exception

class PaymentException(
    code: String,
    message: String,
) : BusinessException(code, message) {

    companion object {
        fun alreadyDone() = PaymentException("PAYMENT_ALREADY_DONE", "이미 완료된 결제입니다")
        fun invalidStatus(status: String) = PaymentException("PAYMENT_INVALID_STATUS", "결제 상태가 올바르지 않습니다: $status")
        fun notFound() = PaymentException("PAYMENT_NOT_FOUND", "결제 정보를 찾을 수 없습니다")
        fun cancelAmountExceeded() = PaymentException("PAYMENT_CANCEL_AMOUNT_EXCEEDED", "취소 금액이 결제 금액을 초과합니다")
        fun tossFailed(message: String) = PaymentException("TOSS_API_FAILED", "토스 API 오류: $message")
    }
}
