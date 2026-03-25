package com.lothuspay.payments.model.deposit.payer;

import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositPayer {

    private String name;
    private String document;
    private String email;

}
