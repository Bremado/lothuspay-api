package com.lothuspay.auth.dto.response.object.profile.billing;

import com.lothuspay.auth.model.accounts.billing.AccountBilling;
import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetAuthProfileBillingDto {

    private String address;
    private String city;
    private String state;
    private String zipCode;

    public GetAuthProfileBillingDto(AccountBilling billing) {
        this.address = billing.getAddress();
        this.city = billing.getCity();
        this.state = billing.getState();
        this.zipCode = billing.getZipCode();
    }
}
