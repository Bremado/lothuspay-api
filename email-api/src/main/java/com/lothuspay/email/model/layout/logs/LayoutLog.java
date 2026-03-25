package com.lothuspay.email.model.layout.logs;

import com.lothuspay.email.model.layout.logs.action.LayoutLogAction;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "layout_logs")
public class LayoutLog {

    @Id
    private String id;
    private String layoutId;

    private LayoutLogAction action;

    private String performedBy;
    private LocalDateTime createdAt;

}
