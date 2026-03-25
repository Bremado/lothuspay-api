package com.lothuspay.auth.model.accounts;

import com.lothuspay.auth.model.accounts.billing.AccountBilling;
import com.lothuspay.auth.model.accounts.document.AccountDocument;
import com.lothuspay.auth.model.accounts.role.AccountRole;
import com.lothuspay.auth.model.accounts.segment.AccountSegment;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "accounts")
public class Account {

    @Id
    private String id;

    private String firstName;
    private String lastName;

    private String email;
    private String password;

    private String phone;

    private Set<AccountRole> roles;

    private AccountDocument document;

    private AccountBilling billing;

    private AccountSegment segment;

    private Boolean active;
    private Boolean deleted;

    private Boolean emailVerified;

    private long created;
    private long updated;

    private long lastLogin;

}
