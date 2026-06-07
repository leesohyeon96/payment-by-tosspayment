package com.shl.payment.common.exception

open class BusinessException(
    val code: String,
    override val message: String,
) : RuntimeException(message)
