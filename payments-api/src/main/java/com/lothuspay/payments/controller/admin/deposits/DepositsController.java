package com.lothuspay.payments.controller.admin.deposits;

import com.lothuspay.payments.dto.response.DtoResponse;
import com.lothuspay.payments.model.deposit.method.DepositRequestMethod;
import com.lothuspay.payments.model.deposit.status.DepositRequestStatus;
import com.lothuspay.payments.service.admin.AdminService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/payments/admin/deposits")
public class DepositsController {

    @Autowired
    private AdminService adminService;

    @GetMapping
    public Mono<DtoResponse> deposits(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "method", required = false) String method,
            @RequestParam(name = "walletId", required = false) String walletId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 50) size = 50;

        DepositRequestStatus statusEnum = status != null ? DepositRequestStatus.valueOf(status) : null;
        DepositRequestMethod methodEnum = method != null ? DepositRequestMethod.valueOf(method) : null;

        return adminService.getDeposits((page-1), size, statusEnum, methodEnum, walletId, startDate, endDate);
    }

    @GetMapping("/{id}")
    public Mono<DtoResponse> getDepositById(@PathVariable("id") String id) {
        return adminService.getDepositById(id);
    }

}
