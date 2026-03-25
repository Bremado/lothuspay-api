package com.lothuspay.wallet.model.promotion;

import com.lothuspay.wallet.model.promotion.type.PromotionType;
import com.lothuspay.wallet.model.promotion.value.PromotionValue;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "promotions")
public class Promotion {

    @Id
    private String id;
    private PromotionType type;

    private String code;

    private PromotionValue value;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
