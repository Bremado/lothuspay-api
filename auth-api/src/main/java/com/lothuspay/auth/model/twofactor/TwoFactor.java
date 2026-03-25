package com.lothuspay.auth.model.twofactor;

import com.lothuspay.auth.model.twofactor.history.TwoFactorHistory;
import com.lothuspay.auth.model.twofactor.temptoken.TwoFactorTempToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "2fa_accounts")
public class TwoFactor {

    @Id
    private String userId;

    private String secret;
    private List<String> backupCodes;
    private List<TwoFactorHistory> history;
    private TwoFactorTempToken tempToken;

    private boolean active;
}
