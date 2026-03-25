package com.lothuspay.payments.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class UserContext {

    private final String userId;
    private final String email;
    private final List<String> roles;
    private final String segment;

}
