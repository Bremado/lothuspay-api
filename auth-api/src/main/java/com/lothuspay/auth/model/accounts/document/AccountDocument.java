package com.lothuspay.auth.model.accounts.document;

import com.lothuspay.auth.service.kyc.records.UploadDocumentResponse;
import lombok.*;

import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDocument {

    private String type;
    private String number;

    private List<UploadDocumentResponse> fileKeys;

    private boolean verified;
    private boolean submitted;

    private long submittedAt;
    
    private String rejectionReason;
    private long reviewedAt;
}
