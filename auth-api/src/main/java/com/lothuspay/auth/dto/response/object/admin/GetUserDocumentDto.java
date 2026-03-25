package com.lothuspay.auth.dto.response.object.admin;

import com.lothuspay.auth.model.accounts.document.AccountDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetUserDocumentDto {

    private String type;
    private String number;

    private List<String> images;

    public GetUserDocumentDto(AccountDocument document, List<String> images) {
        this.type = document.getType();
        this.number = document.getNumber();

        this.images = images;
    }
}
