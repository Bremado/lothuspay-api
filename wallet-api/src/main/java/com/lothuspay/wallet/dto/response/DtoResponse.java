package com.lothuspay.wallet.dto.response;

import com.lothuspay.wallet.dto.response.status.DtoResponseStatus;
import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DtoResponse {

    private DtoResponseStatus status;
    private Object data;
    private String message;

}
