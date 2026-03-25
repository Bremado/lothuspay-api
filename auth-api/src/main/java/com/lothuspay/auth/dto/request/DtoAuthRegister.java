package com.lothuspay.auth.dto.request;

import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DtoAuthRegister {

    private String firstName;
    private String lastName;

    private String email;
    private String password;

    private String phone;

    private String documentType;
    private String documentNumber;

}
