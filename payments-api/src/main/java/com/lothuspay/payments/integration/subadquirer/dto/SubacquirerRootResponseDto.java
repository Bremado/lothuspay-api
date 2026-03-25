package com.lothuspay.payments.integration.subadquirer.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubacquirerRootResponseDto {

    private String message;
    private SubacquirerResponseDto data;

}
