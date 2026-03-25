package com.lothuspay.wallet.controller.admin;

import com.lothuspay.wallet.dto.request.admin.GetWallets;
import com.lothuspay.wallet.dto.request.admin.PostUpdateGlobalTaxes;
import com.lothuspay.wallet.dto.request.admin.PostUpdateUserTaxes;
import com.lothuspay.wallet.dto.response.DtoResponse;
import com.lothuspay.wallet.dto.response.status.DtoResponseStatus;
import com.lothuspay.wallet.pojo.holder.UserContextHolder;
import com.lothuspay.wallet.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/wallet/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserContextHolder contextHolder;

    @GetMapping("/reports/profit")
    public Mono<DtoResponse> getProfitReport(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return contextHolder.current()
                .flatMap(context -> adminService.getProfitReport(startDate, endDate))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }

    @GetMapping("/reports/summary")
    public Mono<DtoResponse> getSummaryReport() {
        return contextHolder.current()
                .flatMap(context -> adminService.getSummaryReport())
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }

    @GetMapping("/config")
    public Mono<DtoResponse> getGlobalConfig() {
        return contextHolder.current()
                .flatMap(context -> adminService.getGlobalConfig())
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }

    @PutMapping("/config/taxes")
    public Mono<DtoResponse> updateGlobalTaxes(@RequestBody PostUpdateGlobalTaxes dto) {
        return contextHolder.current()
                .flatMap(context -> adminService.updateGlobalTaxes(dto))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }

    @PutMapping("/config/taxes/{userId}")
    public Mono<DtoResponse> updateUserTaxes(
            @PathVariable("userId") String userId,
            @RequestBody PostUpdateUserTaxes dto) {
        return contextHolder.current()
                .flatMap(context -> adminService.updateUserTaxes(userId, dto))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }

    @PostMapping("/wallets/search")
    public Mono<DtoResponse> createWallet(@RequestBody GetWallets dto) {
        return contextHolder.current().flatMap(
                context -> adminService.getWallets(dto)
                        .switchIfEmpty(Mono.just(DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_AUTH_001)
                                .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                                .build())));
    }

    @GetMapping("/wallets")
    public Mono<DtoResponse> getWallets(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "minBalance", required = false) java.math.BigDecimal minBalance,
            @RequestParam(name = "maxBalance", required = false) java.math.BigDecimal maxBalance) {
        return contextHolder.current()
                .flatMap(context -> adminService.getWallets(page - 1, limit, userId, minBalance, maxBalance))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }

    @GetMapping("/wallets/{walletId}")
    public Mono<DtoResponse> getWalletById(@PathVariable("walletId") String walletId) {
        return contextHolder.current()
                .flatMap(context -> adminService.getWalletById(walletId))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }

    @GetMapping("/wallets/{walletId}/ledger")
    public Mono<DtoResponse> getWalletLedger(
            @PathVariable("walletId") String walletId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(name = "type", required = false) String type) {
        com.lothuspay.wallet.model.ledger.type.LedgerType typeEnum = type != null ? com.lothuspay.wallet.model.ledger.type.LedgerType.valueOf(type) : null;
        return contextHolder.current()
                .flatMap(context -> adminService.getWalletLedger(walletId, page - 1, limit, startDate, endDate, typeEnum))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }

    @PostMapping("/wallets/{walletId}/adjust")
    public Mono<DtoResponse> adjustBalance(
            @PathVariable("walletId") String walletId,
            @RequestBody com.lothuspay.wallet.dto.request.admin.PostAdjustBalance dto) {
        return contextHolder.current()
                .flatMap(context -> adminService.adjustBalance(walletId, dto))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }

    @PutMapping("/wallets/{walletId}/freeze")
    public Mono<DtoResponse> freezeBalance(
            @PathVariable("walletId") String walletId,
            @RequestBody com.lothuspay.wallet.dto.request.admin.PostFreezeBalance dto) {
        return contextHolder.current()
                .flatMap(context -> adminService.freezeBalance(walletId, dto))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }

}

