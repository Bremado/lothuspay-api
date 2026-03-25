package com.lothuspay.payments.controller.admin.withdraws;

import com.lothuspay.payments.dto.response.DtoResponse;
import com.lothuspay.payments.model.withdraw.status.WithdrawRequestStatus;
import com.lothuspay.payments.service.admin.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/payments/admin/withdraws")
public class WithdrawsController {

    @Autowired
    private AdminService adminService;

    @GetMapping
    public Mono<DtoResponse> withdraws(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "walletId", required = false) String walletId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 50) size = 50;

        WithdrawRequestStatus statusEnum = status != null ? WithdrawRequestStatus.valueOf(status) : null;

        return adminService.getWithdrawals((page-1), size, statusEnum, walletId, startDate, endDate);
    }

    @GetMapping("/{id}")
    public Mono<DtoResponse> getWithdrawalById(@PathVariable("id") String id) {
        return adminService.getWithdrawalById(id);
    }

}
