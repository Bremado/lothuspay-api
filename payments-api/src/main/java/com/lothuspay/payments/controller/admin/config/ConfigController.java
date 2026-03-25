package com.lothuspay.payments.controller.admin.config;

import com.lothuspay.payments.dto.request.admin.PostUpdateConfig;
import com.lothuspay.payments.dto.response.DtoResponse;
import com.lothuspay.payments.pojo.holder.UserContextHolder;
import com.lothuspay.payments.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments/admin/config")
public class ConfigController {

    private final AdminService adminService;
    private final UserContextHolder contextHolder;

    @GetMapping
    public Mono<DtoResponse> config() {
        return contextHolder.current().flatMap(context -> adminService.getConfig());
    }

    @PostMapping("/update")
    public Mono<DtoResponse> updateConfig(@RequestBody PostUpdateConfig dto) {
        return contextHolder.current().flatMap(context -> adminService.updateConfig(dto));
    }
}
