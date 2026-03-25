package com.lothuspay.auth.model.twofactor.history;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorHistory {

    private String code;
    private boolean valid;
    private long timestamp;

}