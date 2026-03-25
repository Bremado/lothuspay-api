package com.lothuspay.auth.dto.response.object.profile.document;

import com.lothuspay.auth.model.accounts.document.AccountDocument;
import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetAuthProfileDocumentDto {

    private String type;
    private String number;

    private boolean verified;
    private boolean submitted;

    private Long submittedAt;

    public GetAuthProfileDocumentDto(AccountDocument document) {
        this.type = document.getType();
        this.number = document.getNumber();
        this.verified = document.isVerified();
        this.submitted = document.isSubmitted();
        this.submittedAt = document.getSubmittedAt();
    }
}
