package com.lothuspay.wallet.model.promotion.value;

import com.lothuspay.wallet.model.promotion.value.type.PromotionValueType;
import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromotionValue {

    private PromotionValueType type;
    private Double amount;

}
