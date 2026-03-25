package com.lothuspay.auth.dto.request;

import com.lothuspay.auth.model.accounts.billing.AccountBilling;
import com.lothuspay.auth.model.accounts.document.AccountDocument;
import com.lothuspay.auth.model.accounts.segment.AccountSegment;
import lombok.*;

import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DtoAuthUserUpdate {

    private String firstName;
    private String lastName;

    private String email;
    private String password;

    private String phone;

    private AccountDocument  document;
    private AccountBilling billing;

    private AccountSegment segment;

    private List<String> roles;

    private boolean emailVerified;

    private boolean active;
}
