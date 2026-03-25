package com.lothuspay.auth.dto.request;

import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DtoAuthValidate {

    private String token;
}
