package com.lothuspay.wallet.pojo;

import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class UserContext {

    private final String userId;
    private final String email;
    private final List<String> roles;

}
