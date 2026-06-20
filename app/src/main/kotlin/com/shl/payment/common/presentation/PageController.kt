package com.shl.payment.common.presentation

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PageController(
    @Value("\${toss.client-key}") private val tossClientKey: String,
) {
    @GetMapping("/") fun index() = "index"
    @GetMapping("/auth/signup", "/signup") fun signup() = "auth/signup"
    @GetMapping("/auth/login", "/login") fun login() = "auth/login"
    @GetMapping("/order/create") fun createOrder() = "order/create"
    @GetMapping("/payment/checkout") fun checkout(model: Model): String {
        model.addAttribute("tossClientKey", tossClientKey)
        return "payment/checkout"
    }
    @GetMapping("/payment/success") fun success() = "payment/success"
    @GetMapping("/payment/fail") fun fail() = "payment/fail"
    @GetMapping("/payment/history") fun history() = "payment/history"
}
