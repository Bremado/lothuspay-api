package com.lothuspay.auth.service.twofactor;

import com.lothuspay.auth.model.accounts.Account;
import com.lothuspay.auth.model.twofactor.TwoFactor;
import com.lothuspay.auth.model.twofactor.temptoken.TwoFactorTempToken;
import com.lothuspay.auth.repository.TwoFactorRepository;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final SystemTimeProvider timeProvider = new SystemTimeProvider();
    private final DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);


    private final TwoFactorRepository repository;

    public TwoFactor create(Account account) {
        var f = repository.findById(account.getId());
        if (f.isPresent()) {
            return f.get();
        }

        var totp = new TwoFactor(
                account.getId(),
                secretGenerator.generate(),
                new ArrayList<>(),
                new ArrayList<>(),
                new TwoFactorTempToken(
                        "",
                        "",
                        0
                ),
                false
        );

        return repository.save(totp);
    }

    public String generateQrCode(String secret, String email) throws Exception {
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer("LothusPay")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(data);

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageData);
    }
    public List<String> generateBackupCodes() {
        List<String> backupCodes = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            String code = String.format("%06d", (int)(Math.random() * 1000000));
            backupCodes.add(code);
        }
        return backupCodes;
    }

    public TwoFactor findByTempToken(String tempToken) {
        return repository.findByTempToken_TempToken(tempToken);
    }

    public boolean verifyCode(TwoFactor twoFactorAccount, String code) {
        return verifier.isValidCode(twoFactorAccount.getSecret(), code);
    }
    public boolean verifyBackupCode(TwoFactor twoFactorAccount, String code) {
        return twoFactorAccount.getBackupCodes().remove(code);
    }

    public void save(TwoFactor twoFactorAccount) {
        repository.save(twoFactorAccount);
    }
    public void delete(TwoFactor twoFactorAccount) {
        repository.delete(twoFactorAccount);
    }
}