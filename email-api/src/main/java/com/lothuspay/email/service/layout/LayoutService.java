package com.lothuspay.email.service.layout;

import com.lothuspay.email.dto.request.PostCreateLayout;
import com.lothuspay.email.dto.response.DtoResponse;
import com.lothuspay.email.dto.response.status.DtoResponseStatus;
import com.lothuspay.email.model.layout.Layout;
import com.lothuspay.email.model.layout.logs.LayoutLog;
import com.lothuspay.email.model.layout.logs.action.LayoutLogAction;
import com.lothuspay.email.model.layout.slug.LayoutSlug;
import com.lothuspay.email.pojo.UserContext;
import com.lothuspay.email.repository.layout.LayoutRepository;
import com.lothuspay.email.repository.layout.log.LayoutLogRepository;
import com.resend.Resend;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LayoutService {

    private final LayoutRepository layoutRepository;
    private final LayoutLogRepository logRepository;

    public Mono<DtoResponse> layouts(int page, int size) {
        var flux = layoutRepository.findAll().skip((long) page * size).take(size);

        return flux.collectList()
                .map(layouts -> DtoResponse.builder()
                        .status(DtoResponseStatus.SUCCESS)
                        .data(layouts)
                        .build())
                .switchIfEmpty(Mono.just(
                        DtoResponse.builder()
                                .status(DtoResponseStatus.SUCCESS)
                                .data("No layouts found")
                                .build()
                ));
    }

    public Mono<DtoResponse> create(UserContext context, PostCreateLayout dto) {
        if (context.getRoles().stream().noneMatch(role -> role.contains("CEO"))) {
            return Mono.just(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_001)
                    .message("Unauthorized to create layout")
                    .build());
        }

        var layout = dto.toLayout();
        if (layout.getSlug() == null) {
            return Mono.just(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_INTERNAL_001)
                    .message("Layout slug cannot be null")
                    .build());
        }

        if (layout.getVersion() == null || layout.getVersion().isBlank()) {
            layout.setVersion("1.0");
        }

        return layoutRepository.findBySlug(layout.getSlug())
                .flatMap(existing ->
                        Mono.just(DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_INTERNAL_001)
                                .message("Layout with the same slug already exists")
                                .build()))
                .switchIfEmpty(
                        layoutRepository.save(layout)
                                .flatMap(saved -> {
                                    var log = LayoutLog.builder()
                                            .id(UUID.randomUUID().toString())
                                            .layoutId(saved.getId())
                                            .action(LayoutLogAction.CREATED)
                                            .performedBy(context.getEmail())
                                            .createdAt(LocalDateTime.now())
                                            .build();

                                    return logRepository.save(log)
                                            .thenReturn(DtoResponse.builder()
                                                    .status(DtoResponseStatus.SUCCESS)
                                                    .data(saved)
                                                    .build());
                                })
                );
    }
    public Mono<DtoResponse> edit(UserContext context, String layoutId, PostCreateLayout dto) {
        if (context.getRoles().stream().noneMatch(role -> role.contains("CEO"))) {
            return Mono.just(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_001)
                    .message("Unauthorized to edit layout")
                    .build());
        }

        if (dto.getSlug() == null) {
            return Mono.just(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_INTERNAL_001)
                    .message("Layout slug cannot be null")
                    .build());
        }

        return layoutRepository.findById(layoutId)
                .switchIfEmpty(Mono.justOrEmpty(null))
                .flatMap(existingLayout ->
                        layoutRepository.findBySlug(dto.getSlug())
                                .filter(other -> !other.getId().equals(layoutId))
                                .flatMap(conflict ->
                                        Mono.just(DtoResponse.builder()
                                                .status(DtoResponseStatus.ERR_INTERNAL_001)
                                                .message("Layout with the same slug already exists")
                                                .build()))
                                .switchIfEmpty(Mono.defer(() -> {
                                    existingLayout.setName(dto.getName());
                                    existingLayout.setSubject(dto.getSubject());
                                    existingLayout.setHtmlContent(dto.getHtmlContent());
                                    existingLayout.setTextContent(dto.getTextContent());
                                    existingLayout.setVersion(dto.getVersion());

                                    return layoutRepository.save(existingLayout)
                                            .flatMap(updated -> {
                                                var log = LayoutLog.builder()
                                                        .id(UUID.randomUUID().toString())
                                                        .layoutId(updated.getId())
                                                        .action(LayoutLogAction.UPDATED)
                                                        .performedBy(context.getEmail())
                                                        .createdAt(LocalDateTime.now())
                                                        .build();

                                                return logRepository.save(log)
                                                        .thenReturn(DtoResponse.builder()
                                                                .status(DtoResponseStatus.SUCCESS)
                                                                .data(updated)
                                                                .build());
                                            });
                                }))
                )
                .switchIfEmpty(Mono.just(
                        DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_INTERNAL_001)
                                .message("Layout not found")
                                .build()
                ));
    }
    public Mono<DtoResponse> delete(UserContext context, String layoutId) {
        if (context.getRoles().stream().noneMatch(role -> role.contains("CEO"))) {
            return Mono.just(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_001)
                    .message("Unauthorized to delete layout")
                    .build());
        }

        return layoutRepository.findById(layoutId)
                .flatMap(layout -> {
                    var log = LayoutLog.builder()
                            .id(UUID.randomUUID().toString())
                            .layoutId(layout.getId())
                            .action(LayoutLogAction.DELETED)
                            .performedBy(context.getEmail())
                            .createdAt(LocalDateTime.now())
                            .build();

                    return layoutRepository.delete(layout)
                            .then(logRepository.save(log))
                            .thenReturn(DtoResponse.builder()
                                    .status(DtoResponseStatus.SUCCESS)
                                    .message("Layout deleted successfully")
                                    .build());
                })
                .switchIfEmpty(Mono.just(
                        DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_INTERNAL_001)
                                .message("Layout not found")
                                .build()
                ));
    }


    public Mono<Layout> findBySlug(LayoutSlug slug) {
        return layoutRepository.findBySlug(slug);
    }
}
