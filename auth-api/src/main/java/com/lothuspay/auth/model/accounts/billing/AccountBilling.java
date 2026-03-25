package com.lothuspay.auth.model.accounts.billing;

import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountBilling {

    private String address;
    private String city;
    private String state;
    private String zipCode;

}
