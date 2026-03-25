package com.lothuspay.auth.service.account;

import com.lothuspay.auth.dto.request.*;
import com.lothuspay.auth.dto.request.admin.PostBlockUser;
import com.lothuspay.auth.dto.request.admin.PostRejectDocument;
import com.lothuspay.auth.dto.request.admin.PostUpdateUserRole;
import com.lothuspay.auth.dto.response.DtoResponse;
import com.lothuspay.auth.dto.response.object.admin.GetDocumentStats;
import com.lothuspay.auth.dto.response.object.admin.GetUserDocumentDto;
import com.lothuspay.auth.dto.response.object.admin.GetUserStats;
import com.lothuspay.auth.dto.response.object.profile.Get2FA;
import com.lothuspay.auth.dto.response.object.profile.GetAuthProfileDto;
import com.lothuspay.auth.dto.response.object.validate.GetValidate;
import com.lothuspay.auth.dto.response.status.DtoResponseStatus;
import com.lothuspay.auth.model.accounts.Account;
import com.lothuspay.auth.model.accounts.billing.AccountBilling;
import com.lothuspay.auth.model.accounts.document.AccountDocument;
import com.lothuspay.auth.model.accounts.role.AccountRole;
import com.lothuspay.auth.model.accounts.segment.AccountSegment;
import com.lothuspay.auth.model.twofactor.history.TwoFactorHistory;
import com.lothuspay.auth.model.twofactor.temptoken.TwoFactorTempToken;
import com.lothuspay.auth.publisher.EventPublisher;
import com.lothuspay.auth.repository.AccountRepository;
import com.lothuspay.auth.service.jwt.JwtService;
import com.lothuspay.auth.service.kyc.KycDocumentService;
import com.lothuspay.auth.service.twofactor.TwoFactorService;
import com.lothuspay.auth.util.AESUtil;
import com.lothuspay.events.dto.email.EmailSend;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Qualifier("customUserDetailsService")
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TwoFactorService twoFactorService;

    @Autowired
    private KycDocumentService kycDocumentService;

    @Autowired
    private EventPublisher publisher;

    public DtoResponse register(DtoAuthRegister dto) {
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[#@!]).{8,}$";

        if (!dto.getPassword().matches(regex)) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_003)
                    .message(DtoResponseStatus.ERR_AUTH_003.getMessage())
                    .build();
        }

        if (!dto.getDocumentType().equalsIgnoreCase("CPF") && !dto.getDocumentType().equalsIgnoreCase("CNPJ")) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_005)
                    .message(DtoResponseStatus.ERR_AUTH_005.getMessage())
                    .build();
        }

        var found = repository.findByEmail(dto.getEmail());

        if (found != null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_001)
                    .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                    .build();
        }

        found = repository.findByPhone(dto.getPhone());

        if (found != null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_002)
                    .message(DtoResponseStatus.ERR_AUTH_002.getMessage())
                    .build();
        }

        found = repository.findAccountByDocument_Number(dto.getDocumentNumber().replace(".", "").replace("-", "").replace("/", ""));

        if (found != null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_004)
                    .message(DtoResponseStatus.ERR_AUTH_004.getMessage())
                    .build();
        }

        if (dto.getDocumentType().equalsIgnoreCase("CPF")) {
            if (!isValidCPF(dto.getDocumentNumber().replace(".", "").replace("-", "").replace("/", ""))) {
                return DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_010)
                        .message(DtoResponseStatus.ERR_AUTH_010.getMessage())
                        .build();
            }
        } else {
            if (!isValidCNPJ(dto.getDocumentNumber().replace(".", "").replace("-", "").replace("/", ""))) {
                return DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_010)
                        .message(DtoResponseStatus.ERR_AUTH_010.getMessage())
                        .build();
            }
        }

        regex = "^(?:\\+55)?[1-9][1-9]\\d{8,9}$";

        if (!dto.getPhone().matches(regex)) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_011)
                    .message(DtoResponseStatus.ERR_AUTH_011.getMessage())
                    .build();
        }

        found = Account.builder()
                .id(UUID.randomUUID().toString())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .document(AccountDocument.builder()
                        .type(dto.getDocumentType().toUpperCase())
                        .number(dto.getDocumentNumber().replace(".", "").replace("-", "").replace("/", ""))
                        .verified(false)
                        .build())
                .roles(Set.of(AccountRole.CLIENT))
                .billing(new AccountBilling(
                        "",
                        "",
                        "",
                        ""
                ))
                .segment(AccountSegment.NONE)
                .active(true)
                .emailVerified(false)
                .deleted(false)
                .created(System.currentTimeMillis())
                .updated(System.currentTimeMillis())
                .lastLogin(System.currentTimeMillis())
                .build();

        repository.save(found);

        publisher.publish("email.send", "AUTH_NEW_ACCOUNT", EmailSend.builder()
                        .from("noreply@lothuspay.com")
                        .to(found.getEmail())
                        .slug("WELCOME_EMAIL")
                        .variables(new HashMap<>())
                .build());

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .message(DtoResponseStatus.SUCCESS.getMessage())
                .build();
    }
    public DtoResponse login(DtoAuthLogin dto) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        var found = repository.findByEmail(dto.getEmail());

        if (found == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build();
        }

        if (!passwordEncoder.matches(dto.getPassword(), found.getPassword())) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_006)
                    .message(DtoResponseStatus.ERR_AUTH_006.getMessage())
                    .build();
        }

        var totp = twoFactorService.create(found);

        if (totp.isActive()) {
            var tokenTemp = UUID.randomUUID().toString();
            totp.setTempToken(
                    new TwoFactorTempToken(
                            tokenTemp,
                            new AESUtil().encrypt(dto.getPassword()),
                            System.currentTimeMillis() + (TimeUnit.MINUTES.toMillis(5))
                    )
            );

            twoFactorService.save(totp);

            return DtoResponse.builder()
                    .status(DtoResponseStatus.SUCCESS)
                    .data(
                            Map.of(
                                    "2fa_required", true,
                                    "temp_token", tokenTemp
                            )).build();
        }


        found.setLastLogin(System.currentTimeMillis());
        repository.save(found);

        var token = token(found, dto.getEmail(), dto.getPassword());

        if (token == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_006)
                    .message(DtoResponseStatus.ERR_AUTH_006.getMessage())
                    .build();
        }

        publisher.publish("email.send", "AUTH_NEW_LOGIN", EmailSend.builder()
                .from("noreply@lothuspay.com")
                .to(found.getEmail())
                .slug("NEW_LOGIN_ALERT")
                .variables(new HashMap<>())
                .build());

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(token)
                .message(DtoResponseStatus.SUCCESS.getMessage())
                .build();
    }

    public DtoResponse validateToken(DtoAuthValidate dto) {
        var email = jwtService.extractUsername(dto.getToken());

        if (email == null || email.isEmpty()) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build();
        }

        try {
            var account = repository.findByEmail(email);
            var userDetails = userDetailsService.loadUserByUsername(email);

            if (account == null) {
                return DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_007)
                        .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                        .build();
            }

            var valid = jwtService.isTokenValid(dto.getToken(), userDetails);

            return DtoResponse.builder()
                    .status(DtoResponseStatus.SUCCESS)
                    .message(DtoResponseStatus.SUCCESS.getMessage())
                    .data(new GetValidate(valid, account.getId(), account.getEmail(), account.getRoles().stream().map(Enum::name).toList(), account.getDocument().isVerified(), account.getEmailVerified(), account.getSegment()))
                    .build();
        } catch (UsernameNotFoundException e) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build();
        }
    }

    public DtoResponse profile(UserDetails userDetails) {
        var found = repository.findByEmail(userDetails.getUsername());

        if (found == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build();
        }

        var totop = twoFactorService.create(found);

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(new GetAuthProfileDto(found, totop)).build();
    }

    public DtoResponse users(UserDetails details) {
        var found = repository.findByEmail(details.getUsername());

        if (found == null || !found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER) && !found.getRoles().contains(AccountRole.SUPPORT)) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build();
        }

        var users = repository.findAll();

        var dtos = users.stream().map(u -> {
            return new GetAuthProfileDto(u, twoFactorService.create(u));
        }).toList();

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .message(DtoResponseStatus.SUCCESS.getMessage())
                .data(dtos)
                .build();
    }
    public DtoResponse updateUser(UserDetails details, String id, DtoAuthUserUpdate dto) {
        var found = repository.findByEmail(details.getUsername());

        if (found == null || !found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER) && !found.getRoles().contains(AccountRole.SUPPORT)) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build();
        }

        var user = repository.findById(id).orElse(null);

        if (user == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build();
        }

        if (dto.getFirstName() != null && !dto.getFirstName().isEmpty()) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null && !dto.getLastName().isEmpty()) {
            user.setLastName(dto.getLastName());
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            user.setPhone(dto.getPhone());
        }

        if (dto.getDocument() != null && !dto.getDocument().getNumber().isEmpty()) {
            user.getDocument().setNumber(dto.getDocument().getNumber());
        }
        if (dto.getDocument() != null && !dto.getDocument().getType().isEmpty()) {
            user.getDocument().setType(dto.getDocument().getType());
        }
        if (dto.getDocument() != null) {
            user.getDocument().setVerified(dto.getDocument().isVerified());
        }

        if (dto.getBilling() != null && !dto.getBilling().getAddress().isEmpty()) {
            user.getBilling().setAddress(dto.getBilling().getAddress());
        }
        if (dto.getBilling() != null &&  !dto.getBilling().getCity().isEmpty()) {
            user.getBilling().setCity(dto.getBilling().getCity());
        }
        if (dto.getBilling() != null &&  !dto.getBilling().getState().isEmpty()) {
            user.getBilling().setState(dto.getBilling().getState());
        }
        if (dto.getBilling() != null && !dto.getBilling().getZipCode().isEmpty()) {
            user.getBilling().setZipCode(dto.getBilling().getZipCode());
        }

        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            var roles = dto.getRoles().stream().map(AccountRole::valueOf);

            user.setRoles(roles.collect(Collectors.toSet()));
        }

        if (dto.getSegment() != null) {
            user.setSegment(dto.getSegment());
        }

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        user.setEmailVerified(dto.isEmailVerified());
        user.setActive(dto.isActive());
        user.setUpdated(System.currentTimeMillis());

        repository.save(user);

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .message(DtoResponseStatus.SUCCESS.getMessage())
                .data(new GetAuthProfileDto(user, twoFactorService.create(user)))
                .build();
    }

    public DtoResponse submitKyc(UserDetails userDetails, MultipartFile front, MultipartFile back, MultipartFile selfie,
                                 String address, String city, String state, String zipCode) {
        var found = repository.findByEmail(userDetails.getUsername());

        if (found == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build();
        }

        if (found.getDocument().isSubmitted()) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_KYC_002)
                    .message(DtoResponseStatus.ERR_KYC_002.getMessage())
                    .build();
        }

        var add = AccountBilling.builder().address(address).city(city).state(state).zipCode(zipCode).build();

        found.setBilling(add);

        var verificationId = "kyc_" + UUID.randomUUID().toString().replaceAll("-", "");

        var f = kycDocumentService.validateFile(front);
        var b = kycDocumentService.validateFile(back);
        var s = kycDocumentService.validateFile(selfie);

        if (!f) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_KYC_003)
                    .message("Arquivo da frente do documento inválido.")
                    .build();
        }

        if (!b) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_KYC_003)
                    .message("Arquivo do verso do documento inválido.")
                    .build();
        }

        if (!s) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_KYC_003)
                    .message("Arquivo da selfie inválido.")
                    .build();
        }

        try {
            var fr = kycDocumentService.uploadDocument(found.getId(), verificationId, front, "Documento (Frente)");
            var br = kycDocumentService.uploadDocument(found.getId(), verificationId, back, "Documento (Verso)");
            var sr = kycDocumentService.uploadDocument(found.getId(), verificationId, selfie, "Documento (Selfie)");

            found.getDocument().setFileKeys(
                    List.of(
                            fr,
                            br,
                            sr
                    )
            );

            found.getDocument().setSubmitted(true);
            found.getDocument().setSubmittedAt(System.currentTimeMillis());

            publisher.publish("email.send", "ACCOUNT_DOCUMENTATION_REQUEST", EmailSend.builder()
                    .from("noreply@lothuspay.com")
                    .to(found.getEmail())
                    .slug("ACCOUNT_DOCUMENTATION_REQUEST")
                    .variables(new HashMap<>())
                    .build());

            repository.save(found);

            return DtoResponse.builder()
                    .status(DtoResponseStatus.SUCCESS)
                    .message("Documentos enviados para análise com sucesso.")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_KYC_100)
                    .message(DtoResponseStatus.ERR_KYC_100.getMessage())
                    .build();
        }
    }

    public DtoResponse twoFactorAuthSetup(UserDetails userDetails) {
        var found = repository.findByEmail(userDetails.getUsername());

        if (found == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build();
        }

        var totp = twoFactorService.create(found);

        if (totp.isActive()) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_023)
                    .message(DtoResponseStatus.ERR_AUTH_023.getMessage())
                    .build();
        }

        try {
            var qrcode = twoFactorService.generateQrCode(totp.getSecret(), found.getEmail());
            return DtoResponse.builder()
                    .status(DtoResponseStatus.SUCCESS)
                    .message(DtoResponseStatus.SUCCESS.getMessage())
                    .data(
                            Map.of(
                                    "secret", totp.getSecret(),
                                    "qrcode", qrcode
                            )
                    )
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_020)
                    .message(DtoResponseStatus.ERR_AUTH_020.getMessage())
                    .build();
        }
    }
    public DtoResponse twoFactorAuthConfirm(String email, String code) {
        var found = repository.findByEmail(email);

        if (found == null) {
            return DtoResponse
                    .builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build();
        }

        var totp = twoFactorService.create(found);

        if (totp.isActive()) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_023)
                    .message(DtoResponseStatus.ERR_AUTH_023.getMessage())
                    .build();
        }

        boolean isValid = twoFactorService.verifyCode(totp, code);

        if (!isValid) {
            totp.getHistory().add(
                    new TwoFactorHistory(
                            code,
                            false,
                            System.currentTimeMillis()
                    )
            );
            twoFactorService.save(totp);
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_021)
                    .message(DtoResponseStatus.ERR_AUTH_021.getMessage())
                    .build();
        }

        totp.setActive(true);
        totp.getHistory().add(
                new TwoFactorHistory(
                        code,
                        true,
                        System.currentTimeMillis()
                )
        );

        var backupCodes = twoFactorService.generateBackupCodes();
        totp.setBackupCodes(backupCodes);

        twoFactorService.save(totp);

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(new Get2FA(
                        totp.getBackupCodes(),
                        totp.isActive()
                )).build();
    }
    public DtoResponse twoFactorAuthVerify(Dto2FAAuth dto) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        var totp = twoFactorService.findByTempToken(dto.getToken());

        if (totp == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .build();
        }

        if (totp.getTempToken().getExpires() < System.currentTimeMillis()) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_008)
                    .message(DtoResponseStatus.ERR_AUTH_008.getMessage())
                    .build();
        }

        boolean isValid = twoFactorService.verifyCode(totp, dto.getCode());

        var found = repository.findById(totp.getUserId()).orElse(null);

        if (found == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build();
        }

        var password = new AESUtil().decrypt(totp.getTempToken().getPassword());

        if (!isValid) {
            boolean isBackupCode = twoFactorService.verifyBackupCode(totp, dto.getCode());
            if (!isBackupCode) {
                totp.getHistory().add(
                        new TwoFactorHistory(
                                dto.getCode(),
                                false,
                                System.currentTimeMillis()
                        )
                );
                twoFactorService.save(totp);
                return DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_021)
                        .message(DtoResponseStatus.ERR_AUTH_021.getMessage()).build();
            } else {
                totp.getBackupCodes().remove(dto.getCode());
                totp.getHistory().add(
                        new TwoFactorHistory(
                                dto.getCode(),
                                true,
                                System.currentTimeMillis()
                        )
                );
                totp.setTempToken(new TwoFactorTempToken("", "", 0));
                twoFactorService.save(totp);

                var token = token(found, found.getEmail(), password);

                return DtoResponse.builder().status(
                        DtoResponseStatus.SUCCESS).data(
                        Map.of(
                                "token", token
                        )).build()
                ;
            }
        }

        totp.getHistory().add(
                new TwoFactorHistory(
                        dto.getCode(),
                        true,
                        System.currentTimeMillis()
                )
        );
        totp.setTempToken(new TwoFactorTempToken("", "", 0));
        twoFactorService.save(totp);

        var token = token(found, found.getEmail(), password);

        return DtoResponse
                .builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(
                        Map.of(
                                "token", token
                        )).build();
    }

    public DtoResponse twoFactorAuthDisable(UserDetails userDetails, String backupCode) {
        var found = repository.findByEmail(userDetails.getUsername());

        if (found == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build();
        }

        var totp = twoFactorService.create(found);

        if (!totp.isActive()) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_024)
                    .message(DtoResponseStatus.ERR_AUTH_024.getMessage())
                    .build();
        }

        boolean isValid = twoFactorService.verifyBackupCode(totp, backupCode);
        if (!isValid) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_021)
                    .message(DtoResponseStatus.ERR_AUTH_021.getMessage())
                    .build();
        }


        twoFactorService.delete(totp);

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .message(DtoResponseStatus.SUCCESS.getMessage())
                .build();
    }
    public String token(Account a, String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (Exception e) {
            return null;
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return jwtService.generateToken(a, userDetails);
    }

    public Account find(String email) {
        return repository.findByEmail(email);
    }

    public DtoResponse getUserById(String userId) {
        var user = repository.findById(userId).orElse(null);
        if (user == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message("Usuário não encontrado.")
                    .build();
        }
        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(new GetAuthProfileDto(user, twoFactorService.create(user)))
                .message("Usuário recuperado com sucesso.")
                .build();
    }
    public DtoResponse getUsersWithFilters(UserDetails details, int page, int limit, String search, String role, Boolean active) {
        var found = repository.findByEmail(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER) && !found.getRoles().contains(AccountRole.SUPPORT))) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build();
        }

        var pageable = org.springframework.data.domain.PageRequest.of(page - 1, limit);
        org.springframework.data.domain.Page<Account> usersPage;

        if (search != null && !search.isEmpty()) {
            if (role != null && !role.isEmpty()) {
                var roleEnum = AccountRole.valueOf(role);
                usersPage = repository.findByEmailOrNameContainingIgnoreCaseAndRolesIn(search, List.of(roleEnum), pageable);
            } else if (active != null) {
                usersPage = repository.findByEmailOrNameContainingIgnoreCaseAndActive(search, active, pageable);
            } else {
                usersPage = repository.findByEmailOrNameContainingIgnoreCase(search, pageable);
            }
        } else if (role != null && !role.isEmpty()) {
            var roleEnum = AccountRole.valueOf(role);
            usersPage = repository.findByRolesContaining(roleEnum, pageable);
        } else if (active != null) {
            usersPage = repository.findByActive(active, pageable);
        } else {
            usersPage = repository.findAll(pageable);
        }

        var dtos = usersPage.getContent().stream()
                .map(u -> new GetAuthProfileDto(u, twoFactorService.create(u)))
                .collect(Collectors.toList());
        var response = new java.util.HashMap<String, Object>();
        response.put("users", dtos);
        response.put("total", usersPage.getTotalElements());
        response.put("page", page);
        response.put("limit", limit);
        response.put("totalPages", usersPage.getTotalPages());

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(response)
                .message("Usuários recuperados com sucesso.")
                .build();
    }

    public DtoResponse blockUser(UserDetails details, String userId, PostBlockUser dto) {
        var found = repository.findByEmail(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER))) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build();
        }

        var user = repository.findById(userId).orElse(null);
        if (user == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message("Usuário não encontrado.")
                    .build();
        }

        // Se blocked = true, então active = false (bloquear)
        // Se blocked = false, então active = true (desbloquear)
        user.setActive(!dto.getBlocked());
        user.setUpdated(System.currentTimeMillis());
        repository.save(user);

        // Mensagem baseada no estado final do usuário (não no DTO)
        String message = user.getActive() ? "Usuário desbloqueado com sucesso." : "Usuário bloqueado com sucesso.";

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(new GetAuthProfileDto(user, twoFactorService.create(user)))
                .message(message)
                .build();
    }
    public DtoResponse updateUserRole(UserDetails details, String userId, PostUpdateUserRole dto) {
        var found = repository.findByEmail(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER))) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build();
        }

        var user = repository.findById(userId).orElse(null);
        if (user == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message("Usuário não encontrado.")
                    .build();
        }

        try {
            var newRole = AccountRole.valueOf(dto.getRole());
            user.setRoles(Set.of(newRole));
            user.setUpdated(System.currentTimeMillis());
            repository.save(user);

            return DtoResponse.builder()
                    .status(DtoResponseStatus.SUCCESS)
                    .data(new GetAuthProfileDto(user, twoFactorService.create(user)))
                    .message("Role do usuário atualizada com sucesso.")
                    .build();
        } catch (IllegalArgumentException e) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_005)
                    .message("Role inválida: " + dto.getRole())
                    .build();
        }
    }
    public DtoResponse getUserStats(UserDetails details) {
        var found = repository.findByEmail(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER) && !found.getRoles().contains(AccountRole.SUPPORT))) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build();
        }

        var allUsers = repository.findAll();
        var totalUsers = (long) allUsers.size();
        var activeUsers = repository.countByActive(true);
        var blockedUsers = repository.countByActive(false);

        var today = System.currentTimeMillis();
        var oneDayAgo = today - (24 * 60 * 60 * 1000);
        var newUsersToday = allUsers.stream()
                .filter(u -> u.getCreated() >= oneDayAgo)
                .count();

        var usersByRole = new java.util.HashMap<String, Long>();
        for (AccountRole role : AccountRole.values()) {
            usersByRole.put(role.name(), repository.countByRolesContaining(role));
        }

        var stats = GetUserStats.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .blockedUsers(blockedUsers)
                .newUsersToday(newUsersToday)
                .usersByRole(usersByRole)
                .build();

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(stats)
                .message("Estatísticas de usuários recuperadas com sucesso.")
                .build();
    }

    private boolean isValidCPF(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1)
            return false;

        int sum = 0;
        for (int i = 0; i < 9; i++)
            sum += (cpf.charAt(i) - '0') * (10 - i);

        int firstDigit = 11 - (sum % 11);
        firstDigit = (firstDigit >= 10) ? 0 : firstDigit;

        sum = 0;
        for (int i = 0; i < 10; i++)
            sum += (cpf.charAt(i) - '0') * (11 - i);

        int secondDigit = 11 - (sum % 11);
        secondDigit = (secondDigit >= 10) ? 0 : secondDigit;

        return cpf.charAt(9) - '0' == firstDigit &&
                cpf.charAt(10) - '0' == secondDigit;
    }
    private boolean isValidCNPJ(String cnpj) {
        if (cnpj == null || !cnpj.matches("\\d{14}") || cnpj.chars().distinct().count() == 1)
            return false;

        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum = 0;
        for (int i = 0; i < 12; i++)
            sum += (cnpj.charAt(i) - '0') * weights1[i];

        int firstDigit = sum % 11;
        firstDigit = (firstDigit < 2) ? 0 : 11 - firstDigit;

        sum = 0;
        for (int i = 0; i < 13; i++)
            sum += (cnpj.charAt(i) - '0') * weights2[i];

        int secondDigit = sum % 11;
        secondDigit = (secondDigit < 2) ? 0 : 11 - secondDigit;

        return cnpj.charAt(12) - '0' == firstDigit &&
                cnpj.charAt(13) - '0' == secondDigit;
    }

    public DtoResponse getPendingDocuments(UserDetails details, int page, int limit) {
        var found = repository.findByEmail(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER) && !found.getRoles().contains(AccountRole.SUPPORT))) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build();
        }

        var pageable = org.springframework.data.domain.PageRequest.of(page - 1, limit);
        
        // Buscar todos os documentos pendentes (submitted=true, verified=false)
        // e filtrar para garantir que não sejam documentos rejeitados
        var allPending = repository.findAll().stream()
                .filter(account -> account.getDocument() != null 
                        && account.getDocument().isSubmitted() 
                        && !account.getDocument().isVerified()
                        && (account.getDocument().getRejectionReason() == null 
                            || account.getDocument().getRejectionReason().isEmpty()))
                .toList();

        // Aplicar paginação manual
        var total = allPending.size();
        var start = (page - 1) * limit;
        var end = Math.min(start + limit, total);
        var paginatedList = allPending.stream()
                .skip(start)
                .limit(limit)
                .toList();

        var dtos = paginatedList.stream()
                .map(GetAuthProfileDto::new)
                .toList();

        var response = new java.util.HashMap<String, Object>();
        response.put("users", dtos);
        response.put("total", total);
        response.put("page", page);
        response.put("limit", limit);
        response.put("totalPages", (int) Math.ceil((double) total / limit));

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(response)
                .message("Documentos pendentes recuperados com sucesso.")
                .build();
    }
    public DtoResponse getUserDocument(UserDetails details, String userId) {
        var found = repository.findByEmail(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER) && !found.getRoles().contains(AccountRole.SUPPORT))) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build();
        }

        var user = repository.findById(userId).orElse(null);
        if (user == null || user.getDocument() == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message("Usuário ou documento não encontrado.")
                    .build();
        }

        var images = new ArrayList<String>();

        for (var fileKey : user.getDocument().getFileKeys()) {
            try {
                var url = kycDocumentService.generatePresignedUrl(fileKey.r2Key());
                images.add(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        var response = new GetUserDocumentDto(
                user.getDocument(),
                images
        );

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(response)
                .message("Documento do usuário recuperado com sucesso.")
                .build();
    }


    public DtoResponse acceptDocument(UserDetails details, String userId) {
        var found = repository.findByEmail(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER) && !found.getRoles().contains(AccountRole.SUPPORT))) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build();
        }

        var user = repository.findById(userId).orElse(null);
        if (user == null || user.getDocument() == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message("Usuário ou documento não encontrado.")
                    .build();
        }

        if (!user.getDocument().isSubmitted()) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_KYC_002)
                    .message("Documento não foi enviado para análise.")
                    .build();
        }

        // Aceitar: apenas mudar verified para true
        user.getDocument().setVerified(true);
        user.getDocument().setRejectionReason(null);
        user.getDocument().setReviewedAt(System.currentTimeMillis());
        user.setUpdated(System.currentTimeMillis());
        repository.save(user);

        publisher.publish("email.send", "ACCOUNT_DOCUMENTATION_SUCCESS", EmailSend.builder()
                .from("noreply@lothuspay.com")
                .to(user.getEmail())
                .slug("ACCOUNT_DOCUMENTATION_SUCCESS")
                .variables(new HashMap<>())
                .build());

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(new GetAuthProfileDto(user))
                .message("Documento aceito com sucesso.")
                .build();
    }

    public DtoResponse rejectDocument(UserDetails details, String userId, PostRejectDocument dto) {
        var found = repository.findByEmail(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER) && !found.getRoles().contains(AccountRole.SUPPORT))) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build();
        }

        var user = repository.findById(userId).orElse(null);
        if (user == null || user.getDocument() == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message("Usuário ou documento não encontrado.")
                    .build();
        }

        if (!user.getDocument().isSubmitted()) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_KYC_002)
                    .message("Documento não foi enviado para análise.")
                    .build();
        }

        // Rejeitar: trocar submitted para false, limpar fileKeys e salvar motivo
        user.getDocument().setSubmitted(false);
        user.getDocument().setVerified(false);
        user.getDocument().setFileKeys(null); // Limpar lista de arquivos
        user.getDocument().setRejectionReason(dto.getReason());
        user.getDocument().setReviewedAt(System.currentTimeMillis());
        user.setUpdated(System.currentTimeMillis());
        repository.save(user);

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(new GetAuthProfileDto(user))
                .message("Documento recusado com sucesso.")
                .build();
    }

    public DtoResponse getDocumentStats(UserDetails details) {
        var found = repository.findByEmail(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER) && !found.getRoles().contains(AccountRole.SUPPORT))) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build();
        }

        var totalPending = repository.countByDocument_SubmittedAndDocument_Verified(true, false);
        var totalVerified = repository.countByDocument_SubmittedAndDocument_Verified(true, true);
        var totalSubmitted = repository.countByDocument_Submitted(true);
        var totalRejected = repository.findAll().stream()
                .filter(account -> account.getDocument() != null 
                        && account.getDocument().isSubmitted() 
                        && !account.getDocument().isVerified()
                        && account.getDocument().getRejectionReason() != null 
                        && !account.getDocument().getRejectionReason().isEmpty())
                .count();

        var stats = GetDocumentStats.builder()
                .totalPending(totalPending)
                .totalVerified(totalVerified)
                .totalRejected(totalRejected)
                .totalSubmitted(totalSubmitted)
                .build();

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(stats)
                .message("Estatísticas de documentos recuperadas com sucesso.")
                .build();
    }

}
