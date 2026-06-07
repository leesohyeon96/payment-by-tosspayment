package com.shl.payment.common.presentation

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PageController {
    @GetMapping("/") fun index() = "index"
    @GetMapping("/auth/signup") fun signup() = "auth/signup"
    @GetMapping("/auth/login") fun login() = "auth/login"
    @GetMapping("/order/create") fun createOrder() = "order/create"
    @GetMapping("/payment/checkout") fun checkout() = "payment/checkout"
    @GetMapping("/payment/success") fun success() = "payment/success"
    @GetMapping("/payment/fail") fun fail() = "payment/fail"
    @GetMapping("/payment/history") fun history() = "payment/history"
}
