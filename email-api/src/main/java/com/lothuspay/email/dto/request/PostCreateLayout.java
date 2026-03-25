package com.lothuspay.email.dto.request;

import com.lothuspay.email.model.layout.Layout;
import com.lothuspay.email.model.layout.slug.LayoutSlug;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateLayout {

    private String name;
    private LayoutSlug slug;

    private String subject;
    private String htmlContent;
    private String textContent;

    private String version;

    private boolean active;

    public Layout toLayout() {
        return Layout.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .slug(slug)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .version(version)
                .active(active)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
