package com.lothuspay.email.controller;

import com.lothuspay.email.dto.request.PostCreateLayout;
import com.lothuspay.email.dto.response.DtoResponse;
import com.lothuspay.email.dto.response.status.DtoResponseStatus;
import com.lothuspay.email.pojo.holder.UserContextHolder;
import com.lothuspay.email.service.layout.LayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/email/layouts")
@RequiredArgsConstructor
public class LayoutController {

    private final LayoutService layoutService;
    private final UserContextHolder contextHolder;

    @GetMapping
    public Mono<DtoResponse> layouts(@RequestParam(name = "page", defaultValue = "1") int page,
                                     @RequestParam(name = "size", defaultValue = "10") int size) {
        if (page < 1) {
            page = 1;
        }

        if (size < 10 || size > 50) {
            size = 10;
        }

        int finalPage = page;
        int finalSize = size;

        return layoutService.layouts((finalPage-1), finalSize);
    }

    @PostMapping("/create")
    public Mono<DtoResponse> createLayout(@RequestBody PostCreateLayout dto) {
        return contextHolder.current().flatMap(context -> layoutService.create(context, dto)).switchIfEmpty(
                Mono.just(
                        DtoResponse.builder()
                                .status(
                                        DtoResponseStatus.ERR_INTERNAL_001
                                )
                                .message("Failed to retrieve user context")
                                .build()
                )
        );
    }

    @PostMapping("/edit/{layoutId}")
    public Mono<DtoResponse> editLayout(@PathVariable("layoutId") String layoutId,
                                        @RequestBody PostCreateLayout dto) {
        return contextHolder.current().flatMap(context -> layoutService.edit(context, layoutId, dto)).switchIfEmpty(
                Mono.just(
                        DtoResponse.builder()
                                .status(
                                        DtoResponseStatus.ERR_INTERNAL_001
                                )
                                .message("Failed to retrieve user context")
                                .build()
                )
        );
    }

    @DeleteMapping("/delete/{layoutId}")
    public Mono<DtoResponse> deleteLayout(@PathVariable("layoutId") String layoutId) {
        return contextHolder.current().flatMap(context -> layoutService.delete(context, layoutId)).switchIfEmpty(
                Mono.just(
                        DtoResponse.builder()
                                .status(
                                        DtoResponseStatus.ERR_INTERNAL_001
                                )
                                .message("Failed to retrieve user context")
                                .build()
                )
        );
    }
}
