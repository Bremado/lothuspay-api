package com.lothuspay.payments.dto.request.callback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubadquirerCallbackDto {

    private String status;
    private BigDecimal value;

}
