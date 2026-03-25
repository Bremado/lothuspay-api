package com.lothuspay.payments.dto.response;

import com.lothuspay.payments.dto.response.status.DtoResponseStatus;
import lombok.*;

@ToString
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DtoResponse {

    private DtoResponseStatus status;
    private Object data;
    private String message;

}
