package com.lothuspay.auth.dto.request;

import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DtoAuthLogin {

    private String email;
    private String password;

}
