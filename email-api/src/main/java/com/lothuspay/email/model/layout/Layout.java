package com.lothuspay.email.model.layout;

import com.lothuspay.email.model.layout.slug.LayoutSlug;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "layouts")
public class Layout {

    @Id
    private String id;

    private String name;
    private LayoutSlug slug;

    private String subject;
    private String htmlContent;
    private String textContent;

    private String version;

    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
