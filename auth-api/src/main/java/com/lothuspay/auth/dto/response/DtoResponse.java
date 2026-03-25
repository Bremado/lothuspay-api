package com.lothuspay.auth.dto.response;

import com.lothuspay.auth.dto.response.status.DtoResponseStatus;
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
