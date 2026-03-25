package com.lothuspay.auth.model.twofactor.temptoken;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorTempToken {

    private String tempToken;
    private String password;
    private long expires;

}