package com.lothuspay.auth.dto.response.object.profile;

import com.lothuspay.auth.dto.response.object.profile.billing.GetAuthProfileBillingDto;
import com.lothuspay.auth.dto.response.object.profile.document.GetAuthProfileDocumentDto;
import com.lothuspay.auth.model.accounts.Account;
import com.lothuspay.auth.model.accounts.role.AccountRole;
import com.lothuspay.auth.model.accounts.segment.AccountSegment;
import com.lothuspay.auth.model.twofactor.TwoFactor;
import lombok.*;

import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetAuthProfileDto {

    private String id;

    private String firstName;
    private String lastName;

    private String email;
    private String phone;

    private List<AccountRole> roles;

    private GetAuthProfileBillingDto  billing;
    private GetAuthProfileDocumentDto document;
    private AccountSegment segment;

    private boolean twoFactorAuthEnabled;

    private boolean emailVerified;

    private boolean active;

    private String created;
    private String updated;

    private String lastLogin;

    public GetAuthProfileDto(Account account, TwoFactor twoFactor) {
        this.id = account.getId();
        this.firstName = account.getFirstName();
        this.lastName = account.getLastName();
        this.email = account.getEmail();
        this.phone = account.getPhone();
        this.roles = account.getRoles().stream().toList();
        this.billing = new GetAuthProfileBillingDto(account.getBilling());
        this.document = new GetAuthProfileDocumentDto(account.getDocument());
        this.segment = account.getSegment();
        this.twoFactorAuthEnabled = twoFactor.isActive();
        this.emailVerified = account.getEmailVerified();
        this.active = account.getActive();
        this.created = String.valueOf(account.getCreated());
        this.updated = String.valueOf(account.getUpdated());
        this.lastLogin = String.valueOf(account.getLastLogin());
    }

    public GetAuthProfileDto(Account account) {
        this.id = account.getId();
        this.firstName = account.getFirstName();
        this.lastName = account.getLastName();
        this.email = account.getEmail();
        this.phone = account.getPhone();
        this.roles = account.getRoles().stream().toList();
        this.billing = new GetAuthProfileBillingDto(account.getBilling());
        this.document = new GetAuthProfileDocumentDto(account.getDocument());
        this.segment = account.getSegment();
        this.emailVerified = account.getEmailVerified();
        this.active = account.getActive();
        this.created = String.valueOf(account.getCreated());
        this.updated = String.valueOf(account.getUpdated());
        this.lastLogin = String.valueOf(account.getLastLogin());
    }
}
