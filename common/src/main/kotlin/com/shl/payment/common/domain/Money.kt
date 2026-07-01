package com.shl.payment.common.domain

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

data class Money(val amount: Long) {
    init {
        require(amount >= 0) { "금액은 0 이상이어야 합니다" }
    }

    operator fun plus(other: Money) = Money(amount + other.amount)
    operator fun times(quantity: Long) = Money(amount * quantity)
}

@Converter(autoApply = false)
class MoneyConverter : AttributeConverter<Money, Long> {
    override fun convertToDatabaseColumn(money: Money?) = money?.amount
    override fun convertToEntityAttribute(amount: Long?) = amount?.let { Money(it) }
}
