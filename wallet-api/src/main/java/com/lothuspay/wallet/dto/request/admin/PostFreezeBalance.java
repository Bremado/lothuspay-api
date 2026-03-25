package com.lothuspay.wallet.dto.request.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostFreezeBalance {

    private Boolean frozen;
    private BigDecimal amount;

}

