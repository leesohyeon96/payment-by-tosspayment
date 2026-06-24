package com.shl.payment.common.domain

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

data class Quantity(val value: Long) {
    init {
        require(value > 0) { "수량은 1 이상이어야 합니다" }
    }
}

@Converter(autoApply = false)
class QuantityConverter : AttributeConverter<Quantity, Long> {
    override fun convertToDatabaseColumn(quantity: Quantity?) = quantity?.value
    override fun convertToEntityAttribute(value: Long?) = value?.let { Quantity(it) }
}
