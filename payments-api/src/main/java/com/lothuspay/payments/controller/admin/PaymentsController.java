package com.lothuspay.payments.controller.admin;

import com.lothuspay.payments.dto.response.DtoResponse;
import com.lothuspay.payments.service.admin.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/payments/admin")
public class PaymentsController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/deposits/stats")
    public Mono<DtoResponse> deposits() {
        return adminService.getDepositsStatistics();
    }

    @GetMapping("/withdraws/stats")
    public Mono<DtoResponse> withdraws() {
        return adminService.getWithdrawalsStatistics();
    }
}
