package com.lothuspay.auth.dto.response.object.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetDocumentStats {

    private Long totalPending;
    private Long totalVerified;
    private Long totalRejected;
    private Long totalSubmitted;

}

